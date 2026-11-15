package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CoachMessage
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.RoadmapMilestone
import com.example.data.model.SkillGap
import com.example.data.model.SkillGapAnalysisResult
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.UserProfile
import com.example.data.repository.AiCareerRepository
import com.example.data.repository.SkillBridgeRepository
import com.example.data.seed.SkillBridgeEcosystemData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Loading : AnalysisUiState
    data class Success(val goalId: String, val targetRole: String) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

sealed interface TutorUiState {
    data object Idle : TutorUiState
    data class Loading(val milestoneId: String) : TutorUiState
    data class Streaming(val milestoneId: String, val text: String) : TutorUiState
    data class Success(val milestoneId: String, val text: String) : TutorUiState
    data class Error(val milestoneId: String, val message: String) : TutorUiState
}

sealed interface CoachStreamState {
    data object Idle : CoachStreamState
    data class Streaming(val mode: String, val text: String) : CoachStreamState
    data class Error(val message: String) : CoachStreamState
}

sealed interface WorkSimState {
    data object Idle : WorkSimState
    data class Streaming(val challengeId: String, val text: String) : WorkSimState
    data class Success(val challengeId: String, val text: String) : WorkSimState
    data class Error(val message: String) : WorkSimState
}

enum class PreviewStateMode {
    NORMAL, LOADING, EMPTY, ERROR, LOCKED
}

@OptIn(ExperimentalCoroutinesApi::class)
class SkillBridgeViewModel(
    private val repo: SkillBridgeRepository,
    private val aiRepo: AiCareerRepository,
    val currentUserId: String,
    private val userDisplayName: String
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = repo.observeProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "profile"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val goalsState: StateFlow<UiState<List<CareerGoal>>> = repo.observeGoals(currentUserId)
        .map<List<CareerGoal>, UiState<List<CareerGoal>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "career goals"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val gapsState: StateFlow<UiState<List<SkillGap>>> = repo.observeGaps(currentUserId)
        .map<List<SkillGap>, UiState<List<SkillGap>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "skill gaps"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val milestonesState: StateFlow<UiState<List<RoadmapMilestone>>> = repo.observeMilestones(currentUserId)
        .map<List<RoadmapMilestone>, UiState<List<RoadmapMilestone>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "roadmap milestones"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val coachMessagesState: StateFlow<UiState<List<CoachMessage>>> = repo.observeCoachMessages(currentUserId)
        .map<List<CoachMessage>, UiState<List<CoachMessage>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "coach history"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val submissionsState: StateFlow<UiState<List<ChallengeSubmission>>> = repo.observeSubmissions(currentUserId)
        .map<List<ChallengeSubmission>, UiState<List<ChallengeSubmission>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "submissions"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val communityPostsState: StateFlow<UiState<List<CommunityPost>>> = repo.observeCommunityPosts()
        .map<List<CommunityPost>, UiState<List<CommunityPost>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "community posts"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val recruitingTeamsState: StateFlow<UiState<List<Team>>> = repo.observeRecruitingTeams()
        .map<List<Team>, UiState<List<Team>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "recruiting teams"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val myTeamsState: StateFlow<UiState<List<Team>>> = repo.observeMyTeams(currentUserId)
        .map<List<Team>, UiState<List<Team>>> { UiState.Success(it) }
        .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "my teams"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostCommentsState: StateFlow<UiState<List<CommunityComment>>> = _selectedPostId
        .flatMapLatest { postId ->
            if (postId.isNullOrBlank() || postId.startsWith("seed_")) {
                flowOf(UiState.Success(SkillBridgeEcosystemData.seedComments[postId].orEmpty()))
            } else {
                repo.observePostComments(postId)
                    .map<List<CommunityComment>, UiState<List<CommunityComment>>> { UiState.Success(it) }
                    .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "comments"))) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Success(emptyList()))

    private val _selectedTeamId = MutableStateFlow<String?>(null)
    val selectedTeamTasksState: StateFlow<UiState<List<TeamTask>>> = _selectedTeamId
        .flatMapLatest { teamId ->
            if (teamId.isNullOrBlank() || teamId.startsWith("seed_")) {
                flowOf(UiState.Success(SkillBridgeEcosystemData.seedTeamTasks[teamId].orEmpty()))
            } else {
                repo.observeTeamTasks(teamId, currentUserId)
                    .map<List<TeamTask>, UiState<List<TeamTask>>> { UiState.Success(it) }
                    .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "team tasks"))) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Success(emptyList()))

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _roleMode = MutableStateFlow(AppRoleMode.LEARNER)
    val roleMode: StateFlow<AppRoleMode> = _roleMode.asStateFlow()

    private val _previewStateMode = MutableStateFlow(PreviewStateMode.NORMAL)
    val previewStateMode: StateFlow<PreviewStateMode> = _previewStateMode.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _latestAnalysis = MutableStateFlow<SkillGapAnalysisResult?>(null)
    val latestAnalysis: StateFlow<SkillGapAnalysisResult?> = _latestAnalysis.asStateFlow()

    private val _tutorState = MutableStateFlow<TutorUiState>(TutorUiState.Idle)
    val tutorState: StateFlow<TutorUiState> = _tutorState.asStateFlow()

    private val _coachStreamState = MutableStateFlow<CoachStreamState>(CoachStreamState.Idle)
    val coachStreamState: StateFlow<CoachStreamState> = _coachStreamState.asStateFlow()

    private val _workSimState = MutableStateFlow<WorkSimState>(WorkSimState.Idle)
    val workSimState: StateFlow<WorkSimState> = _workSimState.asStateFlow()

    private val _savedChallengeIds = MutableStateFlow(setOf("ch_smart_city"))
    val savedChallengeIds: StateFlow<Set<String>> = _savedChallengeIds.asStateFlow()

    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    private val _upvotedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val upvotedPostIds: StateFlow<Set<String>> = _upvotedPostIds.asStateFlow()

    private val _followedTags = MutableStateFlow(setOf("Kotlin", "PostgreSQL", "ReverseEng"))
    val followedTags: StateFlow<Set<String>> = _followedTags.asStateFlow()

    private val _mutedAuthors = MutableStateFlow<Set<String>>(emptySet())
    val mutedAuthors: StateFlow<Set<String>> = _mutedAuthors.asStateFlow()

    private val _notificationChannels = MutableStateFlow(
        mapOf("In-App" to true, "Push" to true, "Email Digest" to false, "Team Mentions" to true)
    )
    val notificationChannels: StateFlow<Map<String, Boolean>> = _notificationChannels.asStateFlow()

    private val _unreadNotificationIds = MutableStateFlow(setOf("n1", "n2"))
    val unreadNotificationIds: StateFlow<Set<String>> = _unreadNotificationIds.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.upsertProfile(
                userId = currentUserId,
                displayName = userDisplayName.ifBlank { "SkillBridge Learner" },
                currentRole = "Backend & Android Builder",
                experienceLevel = "Mid-level"
            )
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
    }

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
    }

    fun setRoleMode(mode: AppRoleMode) {
        _roleMode.value = mode
        viewModelScope.launch {
            val existing = (profileState.value as? UiState.Success)?.data
            if (existing != null) {
                repo.upsertProfile(
                    userId = currentUserId,
                    displayName = existing.displayName,
                    currentRole = existing.currentRole,
                    experienceLevel = existing.experienceLevel,
                    country = existing.country,
                    timezone = existing.timezone,
                    domain = existing.domain,
                    careerLevel = existing.careerLevel,
                    planTier = existing.planTier,
                    roleMode = mode.id,
                    weeklyHours = existing.weeklyHours.toInt(),
                    currentSkills = existing.currentSkills,
                    targetSkills = existing.targetSkills,
                    goalsSummary = existing.goalsSummary,
                    passportPublic = existing.passportPublic,
                    showRepoLinks = existing.showRepoLinks
                )
            }
        }
    }

    fun setPreviewStateMode(mode: PreviewStateMode) {
        _previewStateMode.value = mode
    }

    fun selectPostForComments(postId: String?) {
        _selectedPostId.value = postId
    }

    fun selectTeamForTasks(teamId: String?) {
        _selectedTeamId.value = teamId
    }

    fun markNotificationsRead() {
        _unreadNotificationIds.value = emptySet()
    }

    fun toggleNotificationChannel(channel: String) {
        val current = _notificationChannels.value.toMutableMap()
        current[channel] = !(current[channel] ?: false)
        _notificationChannels.value = current
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun showMessage(msg: String) {
        _actionMessage.value = msg
    }

    fun completeOnboarding(
        displayName: String,
        country: String,
        timezone: String,
        domain: String,
        careerLevel: String,
        currentSkills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ) {
        viewModelScope.launch {
            val res = repo.upsertProfile(
                userId = currentUserId,
                displayName = displayName,
                currentRole = domain,
                experienceLevel = careerLevel,
                country = country,
                timezone = timezone,
                domain = domain,
                careerLevel = careerLevel,
                planTier = "Starter — Free",
                roleMode = _roleMode.value.id,
                weeklyHours = weeklyHours,
                currentSkills = currentSkills,
                targetSkills = targetSkills,
                goalsSummary = goalsSummary,
                passportPublic = true,
                showRepoLinks = true
            )
            _onboardingCompleted.value = true
            if (res.isFailure) {
                _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Profile synced locally"
            } else {
                _actionMessage.value = if (_language.value == AppLanguage.AR) "تم إعداد ملفك بنجاح" else "Onboarding saved!"
            }
        }
    }

    fun updatePrivacySettings(passportPublic: Boolean, showRepoLinks: Boolean) {
        val existing = (profileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repo.upsertProfile(
                userId = currentUserId,
                displayName = existing.displayName,
                currentRole = existing.currentRole,
                experienceLevel = existing.experienceLevel,
                country = existing.country,
                timezone = existing.timezone,
                domain = existing.domain,
                careerLevel = existing.careerLevel,
                planTier = existing.planTier,
                roleMode = existing.roleMode,
                weeklyHours = existing.weeklyHours.toInt(),
                currentSkills = existing.currentSkills,
                targetSkills = existing.targetSkills,
                goalsSummary = existing.goalsSummary,
                passportPublic = passportPublic,
                showRepoLinks = showRepoLinks
            )
            _actionMessage.value = if (_language.value == AppLanguage.AR) "تم تحديث الخصوصية" else "Privacy updated"
        }
    }

    fun resetAnalysisState() {
        _analysisState.value = AnalysisUiState.Idle
    }

    fun analyzeAndSaveCareerBridge(
        targetRole: String,
        experienceLevel: String,
        currentSkills: List<String>,
        backgroundNotes: String,
        weeklyHours: Int
    ) {
        if (targetRole.isBlank()) {
            _analysisState.value = AnalysisUiState.Error("Please enter a target role to analyze.")
            return
        }
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val analysis = aiRepo.analyzeSkillGap(
                    targetRole = targetRole.trim(),
                    experienceLevel = experienceLevel,
                    currentSkills = currentSkills,
                    backgroundNotes = backgroundNotes,
                    weeklyHours = weeklyHours,
                    isArabic = _language.value == AppLanguage.AR
                )
                _latestAnalysis.value = analysis
                val existingIds = (goalsState.value as? UiState.Success)?.data?.map { it.id }.orEmpty()
                val saveResult = repo.saveAnalysisPlan(
                    userId = currentUserId,
                    targetRole = targetRole.trim(),
                    experienceLevel = experienceLevel,
                    weeklyHours = weeklyHours,
                    currentSkills = currentSkills,
                    analysis = analysis,
                    existingGoalIds = existingIds
                )
                if (saveResult.isSuccess) {
                    _analysisState.value = AnalysisUiState.Success(
                        goalId = saveResult.getOrThrow(),
                        targetRole = targetRole.trim()
                    )
                } else {
                    _analysisState.value = AnalysisUiState.Error(
                        saveResult.exceptionOrNull()?.localizedMessage ?: "Failed to save roadmap."
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Skill gap analysis timed out", e)
                _analysisState.value = AnalysisUiState.Error("AI analysis timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Skill gap analysis failed", e)
                _analysisState.value = AnalysisUiState.Error(e.localizedMessage ?: "Skill gap analysis failed.")
            }
        }
    }

    fun toggleSavedChallenge(challengeId: String) {
        val current = _savedChallengeIds.value.toMutableSet()
        if (!current.add(challengeId)) current.remove(challengeId)
        _savedChallengeIds.value = current
    }

    fun toggleSavedPost(postId: String) {
        val current = _savedPostIds.value.toMutableSet()
        if (!current.add(postId)) current.remove(postId)
        _savedPostIds.value = current
    }

    fun toggleFollowTag(tag: String) {
        val current = _followedTags.value.toMutableSet()
        if (!current.add(tag)) current.remove(tag)
        _followedTags.value = current
    }

    fun muteAuthor(authorName: String) {
        _mutedAuthors.value = _mutedAuthors.value + authorName
        _actionMessage.value = if (_language.value == AppLanguage.AR) "تم كتم $authorName" else "Muted $authorName"
    }

    fun containsLeakedSecret(input: String): Boolean {
        val patterns = listOf("AIza", "sk-", "Bearer ", "-----BEGIN", "ghp_", "AKIA")
        return patterns.any { input.contains(it, ignoreCase = false) }
    }

    fun submitChallengeSolution(
        challengeId: String,
        challengeTitle: String,
        companyName: String,
        category: String,
        roleInTeam: String,
        skillsApplied: List<String>,
        repoOrDemoUrl: String,
        understandingReport: String,
        pointsReward: Int,
        rewardAmount: String
    ) {
        if (containsLeakedSecret(understandingReport) || containsLeakedSecret(repoOrDemoUrl)) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "تم حظر الإرسال: تم رصد مفتاح سري أو Token. احذفه أولًا."
            } else {
                "Submission blocked: secret/API key detected. Remove credentials first."
            }
            return
        }
        if (understandingReport.trim().length < 15) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "يرجى كتابة تقرير فهم واضح قبل التسليم."
            } else {
                "Please write a clear Understanding Report before submitting."
            }
            return
        }
        viewModelScope.launch {
            val res = repo.submitChallengeSolution(
                userId = currentUserId,
                challengeId = challengeId,
                challengeTitle = challengeTitle,
                companyName = companyName,
                category = category,
                roleInTeam = roleInTeam,
                skillsApplied = skillsApplied,
                repoOrDemoUrl = repoOrDemoUrl,
                understandingReport = understandingReport,
                pointsReward = pointsReward,
                rewardAmount = rewardAmount
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم رفع التسليم للمراجعة الموثّقة" else "Submitted for rubric & mentor verification!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Submission failed"
            }
        }
    }

    fun toggleSubmissionVisibility(submission: ChallengeSubmission) {
        if (submission.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) "بيانات نموذجية قابلة للتحكم في الخصوصية" else "Privacy preview toggled"
            return
        }
        viewModelScope.launch {
            repo.updateSubmissionPassportVisibility(currentUserId, submission.id, !submission.isPublicOnPassport)
        }
    }

    fun evaluateSubmission(submission: ChallengeSubmission, score: Int, feedback: String, approve: Boolean) {
        if (submission.id.startsWith("seed_")) {
            _actionMessage.value = "Updated rubric evaluation preview ($score%)"
            return
        }
        viewModelScope.launch {
            val res = repo.evaluateSubmissionAsMentor(
                userId = submission.userId.ifBlank { currentUserId },
                submissionId = submission.id,
                status = if (approve) "VERIFIED" else "DISPUTED",
                feedback = feedback,
                score = score,
                pointsAwarded = if (approve) submission.pointsAwarded.toInt().coerceAtLeast(150) else 0,
                rewardStatus = if (approve) "APPROVED" else "DISPUTED"
            )
            _actionMessage.value = if (res.isSuccess) "Evaluation saved" else "Could not save evaluation"
        }
    }

    fun createCommunityPost(postType: String, title: String, body: String, tags: List<String>) {
        if (title.trim().length < 3 || body.trim().length < 3) return
        val profile = (profileState.value as? UiState.Success)?.data
        val authorName = profile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        val authorLevel = profile?.careerLevel?.ifBlank { "Mid-level" } ?: "Mid-level"
        val hasVerified = ((submissionsState.value as? UiState.Success)?.data.orEmpty().any { it.status == "VERIFIED" })
        viewModelScope.launch {
            val res = repo.createCommunityPost(
                authorId = currentUserId,
                authorName = authorName,
                authorLevel = authorLevel,
                authorVerified = hasVerified,
                postType = postType,
                title = title,
                body = body,
                tags = tags
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم نشر مشاركتك في المجتمع" else "Post published to Community"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to publish post"
            }
        }
    }

    fun toggleUpvotePost(post: CommunityPost) {
        val current = _upvotedPostIds.value.toMutableSet()
        val adding = current.add(post.id)
        if (!adding) current.remove(post.id)
        _upvotedPostIds.value = current
        if (!post.id.startsWith("seed_")) {
            viewModelScope.launch {
                val delta = if (adding) 1L else -1L
                repo.updatePostHelpfulCount(post.id, post.helpfulCount + delta)
            }
        }
    }

    fun addComment(post: CommunityPost, parentCommentId: String, body: String) {
        if (body.isBlank()) return
        if (post.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) "تمت إضافة التعليق" else "Reply posted"
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        val authorName = profile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        val authorLevel = profile?.careerLevel?.ifBlank { "Mid-level" } ?: "Mid-level"
        val hasVerified = ((submissionsState.value as? UiState.Success)?.data.orEmpty().any { it.status == "VERIFIED" })
        viewModelScope.launch {
            val res = repo.addComment(
                postId = post.id,
                parentCommentId = parentCommentId,
                authorId = currentUserId,
                authorName = authorName,
                authorLevel = authorLevel,
                authorVerified = hasVerified,
                body = body,
                newCommentCount = post.commentCount + 1L
            )
            if (res.isFailure) {
                _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to reply"
            }
        }
    }

    fun acceptBestAnswer(postId: String, commentId: String) {
        if (postId.startsWith("seed_")) {
            _actionMessage.value = "Best Answer marked"
            return
        }
        viewModelScope.launch {
            val res = repo.acceptBestAnswer(postId, commentId)
            _actionMessage.value = if (res.isSuccess) "Marked as Best Answer" else "Only post author can mark Best Answer"
        }
    }

    fun createTeam(
        name: String,
        description: String,
        domain: String,
        level: String,
        maxMembers: Int,
        requiredSkills: List<String>,
        rolesNeeded: List<String>,
        challengeTitle: String
    ) {
        if (name.trim().length < 2 || description.trim().length < 2) return
        val captainName = (profileState.value as? UiState.Success)?.data?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        viewModelScope.launch {
            val res = repo.createTeam(
                captainId = currentUserId,
                captainName = captainName,
                name = name,
                description = description,
                domain = domain,
                level = level,
                maxMembers = maxMembers,
                requiredSkills = requiredSkills,
                rolesNeeded = rolesNeeded,
                challengeTitle = challengeTitle
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم إنشاء الفريق ونشر إعلان الأعضاء" else "Team created & recruitment ad published"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to create team"
            }
        }
    }

    fun joinTeam(team: Team) {
        if (team.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) "تم إرسال طلب الانضمام إلى ${team.name}" else "Joined ${team.name} workspace!"
            return
        }
        val memberName = (profileState.value as? UiState.Success)?.data?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        viewModelScope.launch {
            val res = repo.joinTeam(team, currentUserId, memberName)
            _actionMessage.value = if (res.isSuccess) "Joined ${team.name}!" else res.exceptionOrNull()?.localizedMessage ?: "Could not join team"
        }
    }

    fun createTeamTask(team: Team, title: String, assigneeName: String, role: String) {
        if (title.isBlank()) return
        if (team.id.startsWith("seed_")) {
            _actionMessage.value = "Task added to sprint board"
            return
        }
        viewModelScope.launch {
            val res = repo.createTeamTask(team, title, assigneeName, role)
            if (res.isFailure) _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Could not add task"
        }
    }

    fun advanceTeamTaskStatus(teamId: String, task: TeamTask) {
        if (teamId.startsWith("seed_")) {
            _actionMessage.value = "Sprint task updated"
            return
        }
        val next = when (task.status) {
            "TODO" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "DONE"
            else -> "TODO"
        }
        viewModelScope.launch {
            repo.updateTeamTaskStatus(teamId, task.id, next)
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String) {
        if (reason.trim().length < 3) return
        viewModelScope.launch {
            val res = repo.submitReport(currentUserId, targetType, targetId, reason)
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم استلام البلاغ لمراجعة المشرفين" else "Report submitted to moderation queue"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Could not submit report"
            }
        }
    }

    fun runWorkSimulation(challengeId: String, challengeTitle: String, userDecision: String) {
        if (userDecision.isBlank()) return
        viewModelScope.launch {
            _workSimState.value = WorkSimState.Streaming(challengeId, "")
            var latest = ""
            try {
                aiRepo.streamWorkSimulationReply(
                    challengeTitle = challengeTitle,
                    userDecision = userDecision,
                    isArabic = _language.value == AppLanguage.AR
                ).collect { partial ->
                    latest = partial
                    _workSimState.value = WorkSimState.Streaming(challengeId, partial)
                }
                _workSimState.value = WorkSimState.Success(challengeId, latest)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _workSimState.value = WorkSimState.Error(e.localizedMessage ?: "Work simulation failed.")
            }
        }
    }

    fun selectActiveGoal(goalId: String) {
        val allIds = (goalsState.value as? UiState.Success)?.data?.map { it.id }.orEmpty()
        if (allIds.isEmpty()) return
        viewModelScope.launch {
            repo.setActiveGoal(currentUserId, goalId, allIds)
        }
    }

    fun toggleMilestone(milestone: RoadmapMilestone) {
        val newCompleted = !milestone.completed
        val allMilestones = (milestonesState.value as? UiState.Success)?.data.orEmpty()
            .filter { it.goalId == milestone.goalId }
        val activeGoal = (goalsState.value as? UiState.Success)?.data?.firstOrNull { it.id == milestone.goalId }
        val updatedScore = if (activeGoal != null && allMilestones.isNotEmpty()) {
            val completedCount = allMilestones.count { if (it.id == milestone.id) newCompleted else it.completed }
            val progressBoost = ((completedCount.toFloat() / allMilestones.size) * 24f).toInt()
            val baseScore = activeGoal.readinessScore.toInt().coerceIn(20, 95)
            val delta = if (newCompleted) (100 - baseScore).coerceAtMost(maxOf(4, progressBoost / allMilestones.size.coerceAtLeast(1))) else -4
            (baseScore + delta).coerceIn(15, 100)
        } else null

        viewModelScope.launch {
            repo.toggleMilestoneCompleted(currentUserId, milestone, newCompleted, updatedScore)
        }
    }

    fun requestMilestoneTutor(targetRole: String, milestone: RoadmapMilestone) {
        viewModelScope.launch {
            _tutorState.value = TutorUiState.Loading(milestone.id)
            var latest = ""
            try {
                aiRepo.streamMilestoneTutor(targetRole, milestone, isArabic = _language.value == AppLanguage.AR).collect { partial ->
                    latest = partial
                    _tutorState.value = TutorUiState.Streaming(milestone.id, partial)
                }
                _tutorState.value = TutorUiState.Success(milestone.id, latest)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _tutorState.value = TutorUiState.Error(milestone.id, e.localizedMessage ?: "Tutor failed")
            }
        }
    }

    fun dismissTutor() {
        _tutorState.value = TutorUiState.Idle
    }

    fun sendCoachMessage(
        mode: String,
        prompt: String,
        activeGoal: CareerGoal?,
        topGaps: List<SkillGap>
    ) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        val goalId = activeGoal?.id?.ifBlank { "general" } ?: "general"
        val history = (coachMessagesState.value as? UiState.Success)?.data.orEmpty().filter { it.mode == mode }

        viewModelScope.launch {
            val saveUser = repo.saveCoachMessage(currentUserId, goalId, mode, "USER", trimmed)
            if (saveUser.isFailure) {
                _coachStreamState.value = CoachStreamState.Error(saveUser.exceptionOrNull()?.localizedMessage ?: "Failed to send")
                return@launch
            }
            _coachStreamState.value = CoachStreamState.Streaming(mode, "")
            var latestReply = ""
            try {
                aiRepo.streamCoachResponse(
                    mode = mode,
                    targetRole = activeGoal?.targetRole.orEmpty(),
                    matchedSkills = activeGoal?.matchedSkills.orEmpty(),
                    topGaps = topGaps.map { "${it.skillName} (${it.priority})" },
                    history = history,
                    userPrompt = trimmed
                ).collect { partial ->
                    latestReply = partial
                    _coachStreamState.value = CoachStreamState.Streaming(mode, partial)
                }
                if (latestReply.isNotBlank()) {
                    repo.saveCoachMessage(currentUserId, goalId, mode, "AI", latestReply)
                }
                _coachStreamState.value = CoachStreamState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _coachStreamState.value = CoachStreamState.Error(e.localizedMessage ?: "Coach failed to respond.")
            }
        }
    }

    fun clearCoachHistory(mode: String) {
        val ids = (coachMessagesState.value as? UiState.Success)?.data.orEmpty().filter { it.mode == mode }.map { it.id }
        viewModelScope.launch { repo.clearCoachMessages(currentUserId, ids) }
    }

    private suspend fun unwrapFirestoreError(error: Throwable, label: String): String {
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Error observing $label", root)
        return root.message ?: "Failed to load $label"
    }

    private companion object {
        const val TAG = "SkillBridgeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
