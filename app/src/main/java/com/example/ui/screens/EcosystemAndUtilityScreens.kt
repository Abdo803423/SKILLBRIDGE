package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CoachMessage
import com.example.data.model.SkillGap
import com.example.data.model.UserProfile
import com.example.data.model.VerificationTier
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.LoadingStateView
import com.example.ui.components.LockedStateView
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.CoachStreamState
import com.example.ui.viewmodel.PreviewStateMode
import com.example.ui.viewmodel.UiState

/**
 * Screen 16: Global Search across Challenges, Contests, Teams, Companies, People, and Community.
 */
@Composable
fun GlobalSearchScreen(
    onOpenChallenge: (String) -> Unit,
    onOpenContests: () -> Unit,
    onOpenCompanies: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }

    val matchingChallenges = SkillBridgeEcosystemData.challenges.filter {
        query.isBlank() ||
            it.title.get(lang).contains(query, ignoreCase = true) ||
            it.companyName.contains(query, ignoreCase = true) ||
            it.skills.any { s -> s.contains(query, ignoreCase = true) }
    }
    val matchingPeople = SkillBridgeEcosystemData.leaderboardEntries.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.domain.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(strings.searchPlaceholder) },
                modifier = Modifier.fillMaxWidth().testTag("global_search_input")
            )
        }

        item { SectionHeaderRow(title = strings.filterChallenges) }
        items(matchingChallenges) { ch ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenChallenge(ch.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(ch.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${ch.companyName} • ${ch.domain}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { SectionHeaderRow(title = strings.top100Title) }
        items(matchingPeople.take(5)) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${p.name} (${p.level})", fontWeight = FontWeight.SemiBold)
                    Text("${p.rating} Rating", color = SkillBridgeColors.BridgeBlueDark)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenContests, modifier = Modifier.weight(1f).height(48.dp)) { Text(strings.contestsTitle) }
                OutlinedButton(onClick = onOpenCompanies, modifier = Modifier.weight(1f).height(48.dp)) { Text(strings.companyDashTitle) }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 17: Notifications + Channel Preferences.
 */
@Composable
fun NotificationsScreen(
    unreadIds: Set<String>,
    channels: Map<String, Boolean>,
    onMarkAllRead: () -> Unit,
    onToggleChannel: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedCat by remember { mutableStateOf("All") }

    val categories = listOf("All", "Evaluation", "Contests", "Teams", "Rewards")
    val list = SkillBridgeEcosystemData.notifications.filter { selectedCat == "All" || it.category == selectedCat }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.notificationsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onMarkAllRead, modifier = Modifier.height(48.dp)) {
                    Text(if (lang == AppLanguage.AR) "تحديد كمقروء" else "Mark Read")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { c ->
                    FilterChip(selected = selectedCat == c, onClick = { selectedCat = c }, label = { Text(c) })
                }
            }
        }

        items(list) { notif ->
            val isUnread = notif.id in unreadIds
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnread) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatusPill(notif.category, if (isUnread) StatusTone.REVIEW else StatusTone.NEUTRAL)
                        Text(notif.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(notif.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(notif.body.get(lang), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (lang == AppLanguage.AR) "تفضيلات قنوات التنبيه" else "Notification Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    channels.forEach { (channel, enabled) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(channel, style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = enabled, onCheckedChange = { onToggleChannel(channel) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 18: Company Partner Directory, Tiered Verification, Transparency & Challenge Publishing.
 */
@Composable
fun CompanyDashboardScreen(
    onReportCompany: (String, String) -> Unit,
    onShowToast: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var newChallengeTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.companyDashTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            InfoNoteBanner(
                text = if (lang == AppLanguage.AR) {
                    "تخضع الشركات لمستويات توثيق متدرجة (غير موثقة / أساسية / تجارية / شريك موثوق) مع عرض سبب التوثيق ونطاقه وتاريخه."
                } else {
                    "Tiered Company Verification ensures transparent trust: Basic Verified companies may run unpaid practice labs; Business Verified & Trusted Partners run escrowed paid challenges."
                }
            )
        }

        items(SkillBridgeEcosystemData.companies) { comp ->
            val tone = when (comp.tier) {
                VerificationTier.TRUSTED -> StatusTone.VERIFIED
                VerificationTier.BUSINESS -> StatusTone.REVIEW
                VerificationTier.BASIC -> StatusTone.PENDING
                VerificationTier.UNVERIFIED -> StatusTone.DISPUTED
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${comp.city} • ${comp.domain}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(if (lang == AppLanguage.AR) comp.tier.badgeAr else comp.tier.badgeEn, tone, Icons.Filled.Verified)
                    }
                    Text(comp.verificationReason.get(lang), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Verified: ${comp.verifiedDate} • ${comp.publishedChallenges} Challenges • ${comp.completionRate}% Completion • ★ ${comp.avgMentorRating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SkillBridgeColors.BridgeBlueDark
                    )
                    Text("Rewards: ${comp.rewardPolicy.get(lang)}", style = MaterialTheme.typography.bodyMedium)
                    Text("IP: ${comp.ipPolicy.get(lang)}", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = { onReportCompany(comp.id, "Verification audit check requested") },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.reportContent)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (lang == AppLanguage.AR) "نشر تحدٍ عملي جديد (بوابة الشركات)" else "Publish Company Challenge Draft", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newChallengeTitle,
                        onValueChange = { newChallengeTitle = it },
                        label = { Text(if (lang == AppLanguage.AR) "عنوان التحدي ومعايير القبول" else "Challenge Title & Rubric Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onShowToast("Challenge draft queued for Company Verification & Rubric audit")
                            newChallengeTitle = ""
                        },
                        enabled = newChallengeTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (lang == AppLanguage.AR) "إرسال للمراجعة والنشر" else "Submit for Escrow & Rubric Review")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 19 & 20: AI Coach + Mock Interview + Mentor Review Studio + Director/Moderator Queue.
 */
@Composable
fun MentorCoachAndAdminScreen(
    initialTab: Int,
    goalsState: UiState<List<CareerGoal>>,
    gapsState: UiState<List<SkillGap>>,
    messagesState: UiState<List<CoachMessage>>,
    coachStreamState: CoachStreamState,
    submissionsState: UiState<List<ChallengeSubmission>>,
    onSendCoachPrompt: (String, String, CareerGoal?, List<SkillGap>) -> Unit,
    onClearCoachHistory: (String) -> Unit,
    onEvaluateSubmission: (ChallengeSubmission, Int, String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("AI Coach") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(strings.roleMentor) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(strings.roleDirector) })
        }

        when (selectedTab) {
            0 -> {
                val goals = (goalsState as? UiState.Success)?.data.orEmpty()
                val activeGoal = goals.firstOrNull { it.isActive } ?: goals.firstOrNull()
                val gaps = (gapsState as? UiState.Success)?.data.orEmpty()
                val messages = (messagesState as? UiState.Success)?.data.orEmpty()
                var mode by remember { mutableStateOf("COACH") }
                var prompt by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = mode == "COACH", onClick = { mode = "COACH" }, label = { Text("Career Coach") })
                        FilterChip(selected = mode == "INTERVIEW", onClick = { mode = "INTERVIEW" }, label = { Text("Mock Interview") })
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { onClearCoachHistory(mode) }) { Text("Clear") }
                    }
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filtered = messages.filter { it.mode == mode }
                        if (filtered.isEmpty()) {
                            item {
                                InfoNoteBanner(
                                    text = if (lang == AppLanguage.AR) "اسأل المدرب الذكي عن خطة المهارات أو اطلب سؤال مقابلة تقنية." else "Ask SkillBridge AI Coach about your skill gaps or launch a live technical mock interview."
                                )
                            }
                        }
                        items(filtered) { msg ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.sender == "USER") SkillBridgeColors.DeepNavy else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    msg.content,
                                    color = if (msg.sender == "USER") SkillBridgeColors.PureWhite else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (coachStreamState is CoachStreamState.Streaming && coachStreamState.mode == mode) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint)) {
                                    Text(coachStreamState.text.ifBlank { "..." }, modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            placeholder = { Text(if (lang == AppLanguage.AR) "اكتب رسالتك للمدرب..." else "Ask AI Coach or answer interview prompt...") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                onSendCoachPrompt(mode, prompt, activeGoal, gaps)
                                prompt = ""
                            },
                            enabled = prompt.isNotBlank() && coachStreamState !is CoachStreamState.Streaming
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
            1 -> {
                val subs = ((submissionsState as? UiState.Success)?.data.orEmpty() + SkillBridgeEcosystemData.seedSubmissions).distinctBy { it.id }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(strings.mentorDashTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(subs) { sub ->
                        var feedback by remember(sub.id) { mutableStateOf(sub.mentorFeedback.ifBlank { "Clean implementation and solid test verification." }) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(sub.challengeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    StatusPill(sub.status, if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.REVIEW)
                                }
                                Text("Understanding Report: ${sub.understandingReport}", style = MaterialTheme.typography.bodyMedium)
                                OutlinedTextField(value = feedback, onValueChange = { feedback = it }, label = { Text("Mentor Verified Feedback") }, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onEvaluateSubmission(sub, 92, feedback, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) { Text("Verify (92%)") }
                                    OutlinedButton(
                                        onClick = { onEvaluateSubmission(sub, 45, feedback, false) },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) { Text("Request Revision") }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(strings.directorAdminTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        InfoNoteBanner(
                            text = if (lang == AppLanguage.AR) "لوحة القادة والمشرفين: مراجعة البلاغات، اعتماد جوائز المسابقات، ومراقبة التزام الهندسة العكسية." else "Director & Moderator Console: audit reports, release contest prize escrows, and verify Reverse Engineering compliance."
                        )
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill("MODERATION QUEUE • 1 OPEN", StatusTone.PENDING)
                                Text("Secret Scanner Guardrail Active", fontWeight = FontWeight.Bold)
                                Text("All Reverse Engineering & Contest submissions undergo automated credential detection + mentor understanding review.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 21 & 22: Privacy Controls + Interactive UI State Previewer + Complete Product Blueprint & MVP Roadmap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrivacyStatesAndBlueprintScreen(
    profile: UserProfile?,
    darkTheme: Boolean,
    previewStateMode: PreviewStateMode,
    onToggleDarkTheme: (Boolean) -> Unit,
    onUpdatePrivacy: (Boolean, Boolean) -> Unit,
    onSelectPreviewMode: (PreviewStateMode) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var passportPublic by remember(profile?.passportPublic) { mutableStateOf(profile?.passportPublic ?: true) }
    var showRepoLinks by remember(profile?.showRepoLinks) { mutableStateOf(profile?.showRepoLinks ?: true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(strings.privacySettingsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        // Granular Privacy Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == AppLanguage.AR) "السجل المهني متاح بالمشاركة" else "Public Experience Passport Link", fontWeight = FontWeight.SemiBold)
                            Text(if (lang == AppLanguage.AR) "إظهار الأدلة الموثقة عند مشاركة الرابط" else "Allow companies to verify code IDs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = passportPublic, onCheckedChange = {
                            passportPublic = it
                            onUpdatePrivacy(it, showRepoLinks)
                        })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == AppLanguage.AR) "إظهار روابط المستودعات (Repo)" else "Show Git Repository Links", fontWeight = FontWeight.SemiBold)
                            Text(if (lang == AppLanguage.AR) "يمكنك إخفاؤها للسرية" else "Hide code links while keeping score & mentor feedback visible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = showRepoLinks, onCheckedChange = {
                            showRepoLinks = it
                            onUpdatePrivacy(passportPublic, it)
                        })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (lang == AppLanguage.AR) "الوضع الداكن (Deep Navy)" else "Deep Navy Dark Theme", fontWeight = FontWeight.SemiBold)
                        Switch(checked = darkTheme, onCheckedChange = onToggleDarkTheme)
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sign_out_btn")
                    ) {
                        Text(strings.signOut, color = SkillBridgeColors.DisputedRed)
                    }
                }
            }
        }

        // Interactive UI State Previewer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.statePreviewTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PreviewStateMode.entries.forEach { mode ->
                            FilterChip(
                                selected = previewStateMode == mode,
                                onClick = { onSelectPreviewMode(mode) },
                                label = { Text(mode.name) }
                            )
                        }
                    }
                    when (previewStateMode) {
                        PreviewStateMode.LOADING -> LoadingStateView()
                        PreviewStateMode.EMPTY -> EmptyStateView(subtitle = "Sample empty state preview", onAction = { onSelectPreviewMode(PreviewStateMode.NORMAL) })
                        PreviewStateMode.ERROR -> ErrorStateView(message = "Sample recoverable network error", onRetry = { onSelectPreviewMode(PreviewStateMode.NORMAL) })
                        PreviewStateMode.LOCKED -> LockedStateView(reason = "Requires verified Challenge #1", actionLabel = "Unlock", onAction = { onSelectPreviewMode(PreviewStateMode.NORMAL) })
                        PreviewStateMode.NORMAL -> StatusPill("Active Live State", StatusTone.VERIFIED, Icons.Filled.CheckCircle)
                    }
                }
            }
        }

        // Product Architecture, Flows & MVP Phase 1/2/3 Blueprint
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.blueprintTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text(
                        "• Core Loop: Skill Gap → Learning → Practical Experience → Evaluation → Verified Evidence → Opportunity\n" +
                            "• 5-Tab Navigation: Home, Discover, Community, Teams, Passport (+ Global Search, Notifications, Role Switcher, AR/EN RTL/LTR)\n" +
                            "• MVP Phase 1 (Launch): Auth + Onboarding + Skill Gap + Challenges + Teams + Experience Passport\n" +
                            "• MVP Phase 2 (Growth): Live Contests + Top-100 Leaderboards + Ethical Reverse Eng Hub + Mentor Studio\n" +
                            "• MVP Phase 3 (Scale): Company Verification Escrow + Talent Pipeline + Skill Evidence Graph",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.InkOnDark
                    )
                    Text(
                        "Open Assumptions:\n" +
                            "1. Financial prize escrow & payouts assume licensed regional PSP integration prior to live cash disbursement.\n" +
                            "2. Automated contest judging runs sandbox test suites; subjective rubric items require human Mentor sign-off.\n" +
                            "3. Reverse Engineering challenges strictly restrict artifacts to authorized sandbox specs with secret-leak scanning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.SoftCyan
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
