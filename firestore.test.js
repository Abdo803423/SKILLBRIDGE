const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowDate() {
  return new Date();
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user goals, submissions, badges, ledger, community posts, or teams", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("goals").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("submissions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("badges").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("pointLedger").get());
  await assertFails(unauthDb.collection("communityPosts").where("isHidden", "==", false).get());
  await assertFails(unauthDb.collection("teams").where("lookingForTeammates", "==", true).get());
});

test("Cross-user isolation: Bob cannot read or write Alice's submissions, badges, or goals", async () => {
  const ts = nowDate();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).collection("submissions").doc("sub_1").set({
      userId: ALICE_UID,
      challengeId: "ch_smart_city",
      challengeTitle: "Smart City Analytics Dashboard",
      companyName: "NilePay Fintech",
      category: "CHALLENGE",
      roleInTeam: "Backend Lead",
      skillsApplied: ["Kotlin", "SQL"],
      understandingReport: "Implemented time-series indexing with verified test coverage.",
      status: "VERIFIED",
      score: 94,
      pointsAwarded: 250,
      rewardStatus: "APPROVED",
      isPublicOnPassport: true,
      verificationCode: "SB-2026-991",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("submissions").doc("sub_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("submissions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("badges").get());
});

test("Persisted preferences, idempotent badges, immutable point ledger amounts, and append-only audit logs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = nowDate();

  // Save profile with AR language & dark theme & onboardingCompleted
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Laila Mansour",
      preferredLanguage: "AR",
      darkTheme: true,
      onboardingCompleted: true,
      savedChallengeIds: ["ch_smart_city"],
      points: 150,
      streakDays: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Deterministic badge write succeeds, and pointsAwarded cannot be tampered with afterwards
  const badgeRef = aliceDb.collection("users").doc(ALICE_UID).collection("badges").doc("first_challenge");
  await assertSucceeds(
    badgeRef.set({
      userId: ALICE_UID,
      badgeId: "first_challenge",
      title: "First Challenge",
      description: "Completed first verified practical challenge",
      criteria: "Submit and pass 1 challenge",
      pointsAwarded: 150,
      isShareable: true,
      unlockedAt: ts,
    })
  );
  await assertFails(badgeRef.update({ pointsAwarded: 9999 }));
  await assertSucceeds(badgeRef.update({ isShareable: false }));

  // Ledger amount is immutable after creation
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("pointLedger").doc("tx_first_challenge");
  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      txId: "tx_first_challenge",
      sourceType: "BADGE",
      sourceId: "first_challenge",
      title: "Unlocked First Challenge Badge",
      points: 150,
      status: "APPROVED",
      reason: "Completed 1st challenge verification",
      createdAt: ts,
    })
  );
  await assertFails(txRef.update({ points: 5000 }));

  // Audit log is append-only (no update)
  const auditRef = aliceDb.collection("users").doc(ALICE_UID).collection("auditLogs").doc("log_1");
  await assertSucceeds(
    auditRef.set({
      userId: ALICE_UID,
      actionType: "BADGE_UNLOCKED",
      summary: "Unlocked First Challenge (+150 pts)",
      actorRole: "LEARNER",
      createdAt: ts,
    })
  );
  await assertFails(auditRef.update({ summary: "Tampered" }));
});

test("Community posts, nested comments, and best-answer authorization", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowDate();

  const postRef = aliceDb.collection("communityPosts").doc("post_1");
  await assertSucceeds(
    postRef.set({
      authorId: ALICE_UID,
      authorName: "Laila Mansour",
      authorLevel: "Mid-level",
      authorVerified: true,
      postType: "Technical Question",
      title: "How to prevent cold-start PERMISSION_DENIED in Firestore?",
      body: "Looking for clean patterns to gate listeners behind Firebase Auth.",
      tags: ["Firebase", "Android"],
      helpfulCount: 1,
      commentCount: 0,
      isHidden: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(bobDb.collection("communityPosts").where("isHidden", "==", false).get());
  await assertFails(bobDb.collection("communityPosts").get());

  const commentRef = bobDb.collection("communityPosts").doc("post_1").collection("comments").doc("c_1");
  await assertSucceeds(
    commentRef.set({
      postId: "post_1",
      parentCommentId: "",
      authorId: BOB_UID,
      authorName: "Omar Al-Harbi",
      authorLevel: "Senior",
      authorVerified: true,
      body: "Gate your root Composable using an AuthStateListener flow.",
      helpfulCount: 0,
      isBestAnswer: false,
      createdAt: ts,
    })
  );

  await assertFails(commentRef.update({ isBestAnswer: true }));
  await assertSucceeds(
    aliceDb.collection("communityPosts").doc("post_1").collection("comments").doc("c_1").update({
      isBestAnswer: true,
    })
  );
});

test("Teams and member-scoped sprint tasks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowDate();

  const teamRef = aliceDb.collection("teams").doc("team_1");
  await assertSucceeds(
    teamRef.set({
      captainId: ALICE_UID,
      captainName: "Laila Mansour",
      name: "Atlas Builders",
      description: "Building smart transit telemetry for MENA Smart City challenge.",
      domain: "Backend",
      level: "Mid-level",
      maxMembers: 4,
      memberIds: [ALICE_UID],
      memberNames: ["Laila Mansour"],
      requiredSkills: ["Kotlin", "SQL"],
      rolesNeeded: ["Backend", "QA"],
      lookingForTeammates: true,
      sprintProgress: 20,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(bobDb.collection("teams").where("lookingForTeammates", "==", true).get());

  const taskRef = aliceDb.collection("teams").doc("team_1").collection("tasks").doc("task_1");
  await assertSucceeds(
    taskRef.set({
      teamId: "team_1",
      memberIds: [ALICE_UID],
      title: "Design REST contract",
      assigneeName: "Laila Mansour",
      role: "Captain",
      status: "IN_PROGRESS",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(bobDb.collection("teams").doc("team_1").collection("tasks").doc("task_1").get());
  await assertFails(bobDb.collection("teams").doc("team_1").collection("tasks").get());
  await assertFails(
    bobDb.collection("teams").doc("team_1").collection("tasks").doc("task_spoof").set({
      teamId: "team_1",
      memberIds: [BOB_UID],
      title: "Spoofed task",
      assigneeName: "Bob",
      role: "Backend",
      status: "TODO",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Reports are append-only and scoped to reporter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowDate();

  const reportRef = aliceDb.collection("reports").doc("rep_1");
  await assertSucceeds(
    reportRef.set({
      reporterId: ALICE_UID,
      targetType: "AI_RESPONSE",
      targetId: "agent_session_1",
      reason: "AI answer gave full solution during assessment challenge.",
      status: "OPEN",
      createdAt: ts,
    })
  );

  await assertFails(bobDb.collection("reports").doc("rep_1").get());
  await assertFails(reportRef.update({ status: "REVIEWED" }));
});

test("Unified cross-device sync collections reject unauthenticated clients and enforce valid schemas", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertFails(unauthDb.collection("projectBriefs").get());
  await assertFails(unauthDb.collection("workSubmissions").get());
  await assertFails(unauthDb.collection("platformAccounts").doc("acc_student_demo").collection("sessions").get());

  await assertSucceeds(
    aliceDb.collection("projectBriefs").doc("brief_sync_1").set({
      id: "brief_sync_1",
      companyId: "comp_nilegrid",
      title: "Cross-Device Sync Analytics",
      approvalStatus: "APPROVED",
    })
  );

  await assertFails(
    aliceDb.collection("workSubmissions").doc("sub_bad").set({
      id: "sub_bad",
      status: "VERIFIED",
      score: 999, // invalid score > 100
    })
  );

  await assertSucceeds(
    aliceDb.collection("platformAccounts").doc("acc_student_demo").collection("sessions").doc("sess_laptop_1").set({
      id: "sess_laptop_1",
      accountId: "acc_student_demo",
      deviceType: "DESKTOP_LAPTOP",
      deviceLabel: "Laptop 1366x768",
      revoked: false,
    })
  );
});
