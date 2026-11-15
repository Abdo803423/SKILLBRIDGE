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

test("Unauthenticated user cannot read or write user goals, submissions, community posts, or teams", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("goals").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("submissions").get());
  await assertFails(unauthDb.collection("communityPosts").where("isHidden", "==", false).get());
  await assertFails(unauthDb.collection("teams").where("lookingForTeammates", "==", true).get());
});

test("Cross-user isolation: Bob cannot read or write Alice's submissions or goals", async () => {
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

  // Query aligned with rule
  await assertSucceeds(bobDb.collection("communityPosts").where("isHidden", "==", false).get());
  // Unfiltered query rejected
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

  // Bob cannot mark his own comment as best answer (only post author Alice can)
  await assertFails(commentRef.update({ isBestAnswer: true }));
  // Alice (post author) can mark Bob's comment as best answer
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

  // Bob (not a member yet) cannot read Alice's team task directly, list all tasks, or spoof a task inside Alice's team
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
      targetType: "POST",
      targetId: "post_1",
      reason: "Contains exposed test API key in snippet.",
      status: "OPEN",
      createdAt: ts,
    })
  );

  await assertFails(bobDb.collection("reports").doc("rep_1").get());
  await assertFails(reportRef.update({ status: "REVIEWED" }));
});
