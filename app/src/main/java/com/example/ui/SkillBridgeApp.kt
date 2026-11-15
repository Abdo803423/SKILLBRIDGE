package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.core.ArStrings
import com.example.core.EnStrings
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.attemptAutoSignIn
import com.example.core.authStateFlow
import com.example.core.layoutDirection
import com.example.core.signOut
import com.example.data.model.CommunityPost
import com.example.data.model.Team
import com.example.data.repository.AiCareerRepository
import com.example.data.repository.SkillBridgeRepository
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.SkillBridgeLogo
import com.example.ui.screens.ChallengeDetailScreen
import com.example.ui.screens.CommunityFeedScreen
import com.example.ui.screens.CompanyDashboardScreen
import com.example.ui.screens.ContestsScreen
import com.example.ui.screens.DiscoverHubScreen
import com.example.ui.screens.ExperiencePassportScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MentorCoachAndAdminScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.PointsWalletScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.PrivacyStatesAndBlueprintScreen
import com.example.ui.screens.ReverseEngineeringHubScreen
import com.example.ui.screens.SkillAnalysisScreen
import com.example.ui.screens.TeamWorkspaceScreen
import com.example.ui.screens.TeamsHubScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeTheme
import com.example.ui.viewmodel.SkillBridgeViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_HOME = "home"
const val ROUTE_DISCOVER = "discover"
const val ROUTE_COMMUNITY = "community"
const val ROUTE_TEAMS = "teams"
const val ROUTE_PASSPORT = "passport"

sealed interface SubRoute {
    data object None : SubRoute
    data object Onboarding : SubRoute
    data object SkillAnalysis : SubRoute
    data class ChallengeDetail(val challengeId: String) : SubRoute
    data object Contests : SubRoute
    data object ReverseEng : SubRoute
    data class PostDetail(val post: CommunityPost) : SubRoute
    data class TeamWorkspace(val team: Team) : SubRoute
    data object Leaderboards : SubRoute
    data object Wallet : SubRoute
    data object Search : SubRoute
    data object Notifications : SubRoute
    data object Companies : SubRoute
    data class CoachMentorAdmin(val initialTab: Int) : SubRoute
    data object PrivacyStatesBlueprint : SubRoute
}

private data class NavTab(
    val route: String,
    val labelEn: String,
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val NAV_TABS = listOf(
    NavTab(ROUTE_HOME, "Home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavTab(ROUTE_DISCOVER, "Discover", "استكشف", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
    NavTab(ROUTE_COMMUNITY, "Community", "المجتمع", Icons.Filled.Forum, Icons.Outlined.Forum, "nav_community"),
    NavTab(ROUTE_TEAMS, "Teams", "الفرق", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_teams"),
    NavTab(ROUTE_PASSPORT, "Passport", "السجل", Icons.Filled.Badge, Icons.Outlined.Badge, "nav_passport")
)

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var unauthLanguage by remember { mutableStateOf(AppLanguage.EN) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        val strings = if (unauthLanguage == AppLanguage.AR) ArStrings else EnStrings
        CompositionLocalProvider(
            LocalLayoutDirection provides unauthLanguage.layoutDirection(),
            LocalAppLanguage provides unauthLanguage,
            LocalAppStrings provides strings
        ) {
            SkillBridgeTheme(darkTheme = false, isArabic = unauthLanguage == AppLanguage.AR) {
                WelcomeScreen(
                    onToggleLanguage = {
                        unauthLanguage = if (unauthLanguage == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
                    },
                    onAuthSuccess = {}
                )
            }
        }
    } else {
        key(user.uid) {
            AuthenticatedSkillBridgeShell(
                user = user,
                onSignOut = {
                    signOut(credentialManager = credentialManager, scope = scope)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedSkillBridgeShell(
    user: FirebaseUser,
    onSignOut: () -> Unit
) {
    val viewModel: SkillBridgeViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SkillBridgeViewModel(
                    repo = SkillBridgeRepository(db),
                    aiRepo = AiCareerRepository(),
                    currentUserId = user.uid,
                    userDisplayName = user.displayName ?: user.email?.substringBefore("@") ?: "Learner"
                )
            }
        }
    )

    val language by viewModel.language.collectAsStateWithLifecycle()
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
    val roleMode by viewModel.roleMode.collectAsStateWithLifecycle()
    val previewStateMode by viewModel.previewStateMode.collectAsStateWithLifecycle()

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val goalsState by viewModel.goalsState.collectAsStateWithLifecycle()
    val gapsState by viewModel.gapsState.collectAsStateWithLifecycle()
    val milestonesState by viewModel.milestonesState.collectAsStateWithLifecycle()
    val coachMessagesState by viewModel.coachMessagesState.collectAsStateWithLifecycle()
    val submissionsState by viewModel.submissionsState.collectAsStateWithLifecycle()
    val communityPostsState by viewModel.communityPostsState.collectAsStateWithLifecycle()
    val recruitingTeamsState by viewModel.recruitingTeamsState.collectAsStateWithLifecycle()
    val myTeamsState by viewModel.myTeamsState.collectAsStateWithLifecycle()
    val selectedCommentsState by viewModel.selectedPostCommentsState.collectAsStateWithLifecycle()
    val selectedTeamTasksState by viewModel.selectedTeamTasksState.collectAsStateWithLifecycle()

    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val latestAnalysis by viewModel.latestAnalysis.collectAsStateWithLifecycle()
    val coachStreamState by viewModel.coachStreamState.collectAsStateWithLifecycle()
    val workSimState by viewModel.workSimState.collectAsStateWithLifecycle()

    val savedChallengeIds by viewModel.savedChallengeIds.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()
    val upvotedPostIds by viewModel.upvotedPostIds.collectAsStateWithLifecycle()
    val followedTags by viewModel.followedTags.collectAsStateWithLifecycle()
    val mutedAuthors by viewModel.mutedAuthors.collectAsStateWithLifecycle()
    val notificationChannels by viewModel.notificationChannels.collectAsStateWithLifecycle()
    val unreadNotificationIds by viewModel.unreadNotificationIds.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    var currentTab by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    var subRoute by remember { mutableStateOf<SubRoute>(SubRoute.None) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val profileData = (profileState as? UiState.Success)?.data
    val displayName = profileData?.displayName?.ifBlank { user.displayName.orEmpty() }
        ?: user.displayName
        ?: "Learner"
    val submissionsList = (submissionsState as? UiState.Success)?.data.orEmpty()

    val strings = if (language == AppLanguage.AR) ArStrings else EnStrings

    CompositionLocalProvider(
        LocalLayoutDirection provides language.layoutDirection(),
        LocalAppLanguage provides language,
        LocalAppStrings provides strings
    ) {
        SkillBridgeTheme(
            darkTheme = darkTheme,
            isArabic = language == AppLanguage.AR
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                if (subRoute !is SubRoute.None) {
                                    IconButton(
                                        onClick = { subRoute = SubRoute.None },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("top_back_btn")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            title = {
                                SkillBridgeLogo(
                                    darkBackground = darkTheme,
                                    compactOnly = useNavigationRail || subRoute !is SubRoute.None
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { subRoute = SubRoute.Search },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("top_search_btn")
                                ) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search")
                                }
                                IconButton(
                                    onClick = { subRoute = SubRoute.Notifications },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("top_notifications_btn")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotificationIds.isNotEmpty()) {
                                                Badge { Text("${unreadNotificationIds.size}") }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Notifications, contentDescription = strings.notificationsTitle)
                                    }
                                }
                                TextButton(
                                    onClick = viewModel::toggleLanguage,
                                    modifier = Modifier.testTag("top_language_toggle")
                                ) {
                                    Text(
                                        strings.languageToggleLabel,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeColors.BridgeBlueDark
                                    )
                                }
                                Box {
                                    IconButton(
                                        onClick = { showRoleMenu = true },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("top_role_menu_btn")
                                    ) {
                                        Icon(
                                            Icons.Filled.AccountCircle,
                                            contentDescription = "Role and Settings",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showRoleMenu,
                                        onDismissRequest = { showRoleMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("${strings.roleLearner}${if (roleMode == AppRoleMode.LEARNER) " ✓" else ""}") },
                                            onClick = {
                                                viewModel.setRoleMode(AppRoleMode.LEARNER)
                                                showRoleMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${strings.roleMentor}${if (roleMode == AppRoleMode.MENTOR) " ✓" else ""}") },
                                            onClick = {
                                                viewModel.setRoleMode(AppRoleMode.MENTOR)
                                                subRoute = SubRoute.CoachMentorAdmin(1)
                                                showRoleMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${strings.roleDirector}${if (roleMode == AppRoleMode.DIRECTOR) " ✓" else ""}") },
                                            onClick = {
                                                viewModel.setRoleMode(AppRoleMode.DIRECTOR)
                                                subRoute = SubRoute.CoachMentorAdmin(2)
                                                showRoleMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${strings.roleCompany}${if (roleMode == AppRoleMode.COMPANY) " ✓" else ""}") },
                                            onClick = {
                                                viewModel.setRoleMode(AppRoleMode.COMPANY)
                                                subRoute = SubRoute.Companies
                                                showRoleMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(strings.privacySettingsTitle) },
                                            onClick = {
                                                subRoute = SubRoute.PrivacyStatesBlueprint
                                                showRoleMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (language == AppLanguage.AR) "إعداد الملف الشخصي" else "Onboarding Setup") },
                                            onClick = {
                                                subRoute = SubRoute.Onboarding
                                                showRoleMenu = false
                                            }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail && subRoute is SubRoute.None) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                NAV_TABS.forEach { tab ->
                                    val selected = currentTab == tab.route
                                    val label = if (language == AppLanguage.AR) tab.labelAr else tab.labelEn
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            currentTab = tab.route
                                            subRoute = SubRoute.None
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                                Spacer(Modifier.size(8.dp))
                                NAV_TABS.forEach { tab ->
                                    val selected = currentTab == tab.route && subRoute is SubRoute.None
                                    val label = if (language == AppLanguage.AR) tab.labelAr else tab.labelEn
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = {
                                            currentTab = tab.route
                                            subRoute = SubRoute.None
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (val sr = subRoute) {
                                SubRoute.None -> {
                                    when (currentTab) {
                                        ROUTE_HOME -> HomeDashboardScreen(
                                            displayName = displayName,
                                            profile = profileData,
                                            goalsState = goalsState,
                                            submissions = submissionsList,
                                            previewStateMode = previewStateMode,
                                            onRetry = { viewModel.setPreviewStateMode(com.example.ui.viewmodel.PreviewStateMode.NORMAL) },
                                            onOpenSkillAnalysis = { subRoute = SubRoute.SkillAnalysis },
                                            onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                                            onOpenContests = { subRoute = SubRoute.Contests },
                                            onOpenReverseEng = { subRoute = SubRoute.ReverseEng },
                                            onOpenLeaderboards = { subRoute = SubRoute.Leaderboards },
                                            onOpenWallet = { subRoute = SubRoute.Wallet },
                                            onOpenCompanies = { subRoute = SubRoute.Companies },
                                            onOpenCoach = { subRoute = SubRoute.CoachMentorAdmin(0) },
                                            onOpenOnboarding = { subRoute = SubRoute.Onboarding },
                                            onOpenBlueprint = { subRoute = SubRoute.PrivacyStatesBlueprint },
                                            onOpenPassport = { currentTab = ROUTE_PASSPORT }
                                        )
                                        ROUTE_DISCOVER -> DiscoverHubScreen(
                                            savedChallengeIds = savedChallengeIds,
                                            onToggleSave = viewModel::toggleSavedChallenge,
                                            onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                                            onOpenContests = { subRoute = SubRoute.Contests },
                                            onOpenReverseEng = { subRoute = SubRoute.ReverseEng },
                                            onOpenTeams = { currentTab = ROUTE_TEAMS }
                                        )
                                        ROUTE_COMMUNITY -> CommunityFeedScreen(
                                            postsState = communityPostsState,
                                            upvotedPostIds = upvotedPostIds,
                                            savedPostIds = savedPostIds,
                                            followedTags = followedTags,
                                            mutedAuthors = mutedAuthors,
                                            onToggleUpvote = viewModel::toggleUpvotePost,
                                            onToggleSavePost = viewModel::toggleSavedPost,
                                            onToggleFollowTag = viewModel::toggleFollowTag,
                                            onMuteAuthor = viewModel::muteAuthor,
                                            onCreatePost = viewModel::createCommunityPost,
                                            onReportPost = { postId, reason ->
                                                viewModel.submitReport("POST", postId, reason)
                                            },
                                            onOpenPost = { post ->
                                                viewModel.selectPostForComments(post.id)
                                                subRoute = SubRoute.PostDetail(post)
                                            }
                                        )
                                        ROUTE_TEAMS -> TeamsHubScreen(
                                            currentUserId = viewModel.currentUserId,
                                            myTeamsState = myTeamsState,
                                            recruitingTeamsState = recruitingTeamsState,
                                            onCreateTeam = viewModel::createTeam,
                                            onJoinTeam = viewModel::joinTeam,
                                            onOpenTeamWorkspace = { team ->
                                                viewModel.selectTeamForTasks(team.id)
                                                subRoute = SubRoute.TeamWorkspace(team)
                                            }
                                        )
                                        ROUTE_PASSPORT -> ExperiencePassportScreen(
                                            displayName = displayName,
                                            profile = profileData,
                                            submissionsState = submissionsState,
                                            onToggleSubmissionVisibility = viewModel::toggleSubmissionVisibility,
                                            onOpenWallet = { subRoute = SubRoute.Wallet },
                                            onOpenLeaderboards = { subRoute = SubRoute.Leaderboards },
                                            onOpenPrivacyStates = { subRoute = SubRoute.PrivacyStatesBlueprint },
                                            onShareFeedbackMessage = viewModel::showMessage
                                        )
                                    }
                                }
                                SubRoute.Onboarding -> OnboardingWizardScreen(
                                    initialName = displayName,
                                    currentRoleMode = roleMode,
                                    onSelectRoleMode = viewModel::setRoleMode,
                                    onComplete = { name, country, tz, dom, lvl, cur, tgt, goal, hrs ->
                                        viewModel.completeOnboarding(name, country, tz, dom, lvl, cur, tgt, goal, hrs)
                                        subRoute = SubRoute.None
                                    },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.SkillAnalysis -> SkillAnalysisScreen(
                                    goalsState = goalsState,
                                    gapsState = gapsState,
                                    milestonesState = milestonesState,
                                    analysisState = analysisState,
                                    latestAnalysis = latestAnalysis,
                                    onRunAnalysis = viewModel::analyzeAndSaveCareerBridge,
                                    onToggleMilestone = viewModel::toggleMilestone,
                                    onProveSkill = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                is SubRoute.ChallengeDetail -> {
                                    val ch = SkillBridgeEcosystemData.challenges.firstOrNull { it.id == sr.challengeId }
                                        ?: SkillBridgeEcosystemData.challenges.first()
                                    ChallengeDetailScreen(
                                        challenge = ch,
                                        isSaved = ch.id in savedChallengeIds,
                                        workSimState = workSimState,
                                        containsSecret = viewModel::containsLeakedSecret,
                                        onToggleSave = { viewModel.toggleSavedChallenge(ch.id) },
                                        onRunWorkSim = viewModel::runWorkSimulation,
                                        onSubmitSolution = { role, url, report, skills ->
                                            viewModel.submitChallengeSolution(
                                                challengeId = ch.id,
                                                challengeTitle = ch.title.en,
                                                companyName = ch.companyName,
                                                category = if (ch.category.name == "REVERSE_ENG") "REVERSE_ENG" else "CHALLENGE",
                                                roleInTeam = role,
                                                skillsApplied = skills,
                                                repoOrDemoUrl = url,
                                                understandingReport = report,
                                                pointsReward = ch.pointsReward,
                                                rewardAmount = ch.prizePool
                                            )
                                        },
                                        onOpenTeams = {
                                            subRoute = SubRoute.None
                                            currentTab = ROUTE_TEAMS
                                        },
                                        onBack = { subRoute = SubRoute.None }
                                    )
                                }
                                SubRoute.Contests -> ContestsScreen(
                                    onJoinTeamForContest = {
                                        subRoute = SubRoute.None
                                        currentTab = ROUTE_TEAMS
                                    },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.ReverseEng -> ReverseEngineeringHubScreen(
                                    containsSecret = viewModel::containsLeakedSecret,
                                    onSubmitLab = { lab, role, url, report ->
                                        viewModel.submitChallengeSolution(
                                            challengeId = lab.id,
                                            challengeTitle = lab.title.en,
                                            companyName = lab.companyName,
                                            category = "REVERSE_ENG",
                                            roleInTeam = role,
                                            skillsApplied = lab.skills,
                                            repoOrDemoUrl = url,
                                            understandingReport = report,
                                            pointsReward = lab.pointsReward,
                                            rewardAmount = lab.prizePool
                                        )
                                    },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                is SubRoute.PostDetail -> PostDetailScreen(
                                    post = sr.post,
                                    currentUserId = viewModel.currentUserId,
                                    commentsState = selectedCommentsState,
                                    onAddComment = { parentId, body ->
                                        viewModel.addComment(sr.post, parentId, body)
                                    },
                                    onAcceptBestAnswer = { commentId ->
                                        viewModel.acceptBestAnswer(sr.post.id, commentId)
                                    },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                is SubRoute.TeamWorkspace -> TeamWorkspaceScreen(
                                    team = sr.team,
                                    tasksState = selectedTeamTasksState,
                                    onCreateTask = { title, assignee, role ->
                                        viewModel.createTeamTask(sr.team, title, assignee, role)
                                    },
                                    onAdvanceTask = { task ->
                                        viewModel.advanceTeamTaskStatus(sr.team.id, task)
                                    },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.Leaderboards -> LeaderboardScreen(
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.Wallet -> PointsWalletScreen(
                                    pointsBalance = profileData?.points ?: 1280L,
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.Search -> GlobalSearchScreen(
                                    onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                                    onOpenContests = { subRoute = SubRoute.Contests },
                                    onOpenCompanies = { subRoute = SubRoute.Companies },
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.Notifications -> NotificationsScreen(
                                    unreadIds = unreadNotificationIds,
                                    channels = notificationChannels,
                                    onMarkAllRead = viewModel::markNotificationsRead,
                                    onToggleChannel = viewModel::toggleNotificationChannel,
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.Companies -> CompanyDashboardScreen(
                                    onReportCompany = { compId, reason ->
                                        viewModel.submitReport("COMPANY", compId, reason)
                                    },
                                    onShowToast = viewModel::showMessage,
                                    onBack = { subRoute = SubRoute.None }
                                )
                                is SubRoute.CoachMentorAdmin -> MentorCoachAndAdminScreen(
                                    initialTab = sr.initialTab,
                                    goalsState = goalsState,
                                    gapsState = gapsState,
                                    messagesState = coachMessagesState,
                                    coachStreamState = coachStreamState,
                                    submissionsState = submissionsState,
                                    onSendCoachPrompt = viewModel::sendCoachMessage,
                                    onClearCoachHistory = viewModel::clearCoachHistory,
                                    onEvaluateSubmission = viewModel::evaluateSubmission,
                                    onBack = { subRoute = SubRoute.None }
                                )
                                SubRoute.PrivacyStatesBlueprint -> PrivacyStatesAndBlueprintScreen(
                                    profile = profileData,
                                    darkTheme = darkTheme,
                                    previewStateMode = previewStateMode,
                                    onToggleDarkTheme = viewModel::setDarkTheme,
                                    onUpdatePrivacy = viewModel::updatePrivacySettings,
                                    onSelectPreviewMode = viewModel::setPreviewStateMode,
                                    onSignOut = onSignOut,
                                    onBack = { subRoute = SubRoute.None }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
