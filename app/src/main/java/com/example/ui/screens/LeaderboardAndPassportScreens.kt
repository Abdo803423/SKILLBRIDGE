package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.model.ChallengeSubmission
import com.example.data.model.UserProfile
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.LevelAndPlanBadgeRow
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.UiState

/**
 * Screen 13: Multi-Dimensional Top-100 Leaderboards.
 */
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val scopes = listOf("Global", "MENA", "Egypt", "Saudi Arabia", "UAE", "Jordan", "Junior", "Mid-level", "Senior", "Teams", "Contests", "Community")
    var selectedScope by remember { mutableStateOf("MENA") }
    var selectedDomain by remember { mutableStateOf("All") }

    val filtered = remember(selectedScope, selectedDomain) {
        val base = SkillBridgeEcosystemData.leaderboardEntries.filter { entry ->
            val scopeOk = when (selectedScope) {
                "Global" -> true
                "MENA" -> entry.region == "MENA"
                "Egypt", "Saudi Arabia", "UAE", "Jordan" -> entry.country == selectedScope
                "Junior", "Mid-level", "Senior" -> entry.level == selectedScope
                "Teams" -> entry.isTeam
                else -> !entry.isTeam
            }
            val domainOk = selectedDomain == "All" || entry.domain == selectedDomain
            scopeOk && domainOk
        }
        val list = if (base.isEmpty()) SkillBridgeEcosystemData.leaderboardEntries.take(6) else base
        list.sortedByDescending { it.rating }.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.leaderboardTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(strings.top100Title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                scopes.forEach { sc ->
                    FilterChip(selected = selectedScope == sc, onClick = { selectedScope = sc }, label = { Text(sc) })
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (listOf("All") + SkillBridgeEcosystemData.domains).forEach { dom ->
                    FilterChip(selected = selectedDomain == dom, onClick = { selectedDomain = dom }, label = { Text(dom) })
                }
            }
        }

        // Top 3 Podium in SkillBridge Brand Palette (#0B1F38, #00B4DB, #A6E22E)
        if (filtered.size >= 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PodiumColumn(entry = filtered[1], medal = "#2", accent = SkillBridgeColors.SoftCyan, heightDp = 82, modifier = Modifier.weight(1f))
                        PodiumColumn(entry = filtered[0], medal = "#1", accent = SkillBridgeColors.LimeGreen, heightDp = 104, modifier = Modifier.weight(1.1f))
                        PodiumColumn(entry = filtered[2], medal = "#3", accent = SkillBridgeColors.BridgeBlue, heightDp = 70, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.ratingFormulaTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.DeepNavy)
                    Text(
                        if (lang == AppLanguage.AR) {
                            "التقييم (Rating) = أداء المسابقات والتحديات الموثّقة (٦٠٪) + تقييم المرشدين (٢٥٪) + إجابات المجتمع المفيدة (١٥٪). النقاط (Points) تراكمية ولا تنخفض."
                        } else {
                            "Rating = Verified Challenges & Contests (60%) + Rubric Mentor Score (25%) + Community Accepted Answers (15%). Points accumulate cumulatively."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.DeepNavy
                    )
                }
            }
        }

        items(filtered) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = if (entry.rank <= 3) SkillBridgeColors.DeepNavy else SkillBridgeColors.SurfaceSubtle,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${entry.rank}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (entry.rank <= 3) SkillBridgeColors.LimeGreen else SkillBridgeColors.DeepNavy
                                )
                            }
                        }
                        Column {
                            Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${entry.country} • ${entry.level} • ${entry.verifiedCount} verified", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${entry.rating}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeColors.BridgeBlueDark)
                        Text(
                            text = "${entry.points} pts (${if (entry.rankDelta >= 0) "+${entry.rankDelta}" else "${entry.rankDelta}"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.rankDelta >= 0) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.DisputedRed
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PodiumColumn(
    entry: com.example.data.model.LeaderboardEntry,
    medal: String,
    accent: androidx.compose.ui.graphics.Color,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(entry.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${entry.rating}", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .background(accent.copy(alpha = 0.22f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(medal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

/**
 * Screen 14: Experience Passport + Skill Evidence Graph matching Phone #4 in Poster.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExperiencePassportScreen(
    displayName: String,
    profile: UserProfile?,
    submissionsState: UiState<List<ChallengeSubmission>>,
    onToggleSubmissionVisibility: (ChallengeSubmission) -> Unit,
    onOpenWallet: () -> Unit,
    onOpenLeaderboards: () -> Unit,
    onOpenPrivacyStates: () -> Unit,
    onShareFeedbackMessage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val liveSubs = (submissionsState as? UiState.Success)?.data.orEmpty()
    val allSubs = remember(liveSubs) {
        (liveSubs + SkillBridgeEcosystemData.seedSubmissions).distinctBy { it.id }
    }

    var selectedCertificate by remember { mutableStateOf<ChallengeSubmission?>(null) }
    val showRepos = profile?.showRepoLinks ?: true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.passportTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = {
                        onShareFeedbackMessage(
                            if (lang == AppLanguage.AR) "تم نسخ رابط التحقق: skillbridge.dev/passport/${profile?.userId?.take(8) ?: "sb"}"
                            else "Copied Verified Passport link: skillbridge.dev/passport/${profile?.userId?.take(8) ?: "sb"}"
                        )
                    },
                    modifier = Modifier.height(48.dp).testTag("share_passport_btn")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.sharePassport)
                }
            }
        }

        // Poster Phone #4 Hero Verified Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SkillBridgeRadius.lg),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(SkillBridgeColors.DeepNavy, SkillBridgeColors.DeepNavyElevated)))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = CircleShape, color = SkillBridgeColors.BridgeBlue.copy(alpha = 0.25f), modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = SkillBridgeColors.SoftCyan, modifier = Modifier.size(32.dp))
                        }
                    }
                    Text(strings.verifiedExperience, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text("$displayName • ${profile?.careerLevel ?: "Mid-level"}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.SoftCyan)
                    Text(
                        if (lang == AppLanguage.AR) "كل بطاقة أدناه موثّقة بتسليم فعلي وتقييم مرشد ورمز تحقق" else "Every badge below links directly to deliverables, mentor feedback, and a verification ID.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkillBridgeColors.InkMutedOnDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            LevelAndPlanBadgeRow(
                careerLevel = profile?.careerLevel?.ifBlank { "Mid-level" } ?: "Mid-level",
                planTier = profile?.planTier?.ifBlank { "Starter — Free" } ?: "Starter — Free"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenWallet, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.walletTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onOpenLeaderboards, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.leaderboardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = onOpenPrivacyStates, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(strings.privacySettingsTitle)
            }
        }

        // Verified Experience Cards (Poster Phone #4 Style)
        items(allSubs, key = { it.id }) { sub ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sub.challengeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${sub.companyName} • ${sub.roleInTeam}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                        }
                        IconButton(onClick = { onToggleSubmissionVisibility(sub) }) {
                            Icon(
                                if (sub.isPublicOnPassport) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle passport visibility"
                            )
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sub.skillsApplied.forEach { sk ->
                            Surface(shape = RoundedCornerShape(SkillBridgeRadius.pill), color = SkillBridgeColors.SoftCyanTint) {
                                Text(sk, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.DeepNavy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusPill(sub.status, if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.REVIEW)
                        StatusPill("ID: ${sub.verificationCode}", StatusTone.NEUTRAL)
                    }

                    if (sub.mentorFeedback.isNotBlank()) {
                        Text(strings.mentorFeedback, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("\"${sub.mentorFeedback}\"", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = SkillBridgeColors.PendingAmber, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (showRepos && sub.repoOrDemoUrl.isNotBlank()) {
                        Text(sub.repoOrDemoUrl, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                    }

                    OutlinedButton(
                        onClick = { selectedCertificate = sub },
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (lang == AppLanguage.AR) "عرض الشهادة وسجل التحقق" else "View Certificate & Proof")
                    }
                }
            }
        }

        // Skill Evidence Graph
        item {
            SectionHeaderRow(title = strings.skillEvidenceGraph)
        }
        items(SkillBridgeEcosystemData.evidenceNodes) { node ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (node.isVerified) SkillBridgeColors.VerifiedGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Hub,
                        contentDescription = null,
                        tint = if (node.isVerified) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.PendingAmber
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${node.skill} (${node.level})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("→ ${node.proofTitle.get(lang)}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                        Text("${node.evaluator} • ${node.verificationId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(
                        if (node.isVerified) strings.verifiedBadge else strings.selfClaimedBadge,
                        if (node.isVerified) StatusTone.VERIFIED else StatusTone.PENDING
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    selectedCertificate?.let { cert ->
        AlertDialog(
            onDismissRequest = { selectedCertificate = null },
            title = { Text(cert.challengeTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Verification Code: ${cert.verificationCode}", fontWeight = FontWeight.Bold)
                    Text("Partner: ${cert.companyName}")
                    Text("Role: ${cert.roleInTeam}")
                    Text("Score: ${cert.score}% • +${cert.pointsAwarded} pts")
                    Text("Understanding Report: ${cert.understandingReport}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCertificate = null }) { Text("Close") }
            }
        )
    }
}

/**
 * Screen 15: Points & Rewards Transactions Ledger.
 */
@Composable
fun PointsWalletScreen(
    pointsBalance: Long,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.walletTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.pointsLabel, style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.SoftCyan)
                    Text("$pointsBalance PTS", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text(
                        if (lang == AppLanguage.AR) "صافي المكافآت المعتمدة: ٧٥٠$ • قيد المراجعة: ٢٥٠$" else "Approved & Paid Bounties: $750 • Under Review: $250",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.LimeGreen
                    )
                }
            }
        }
        item {
            InfoNoteBanner(text = strings.prizePoolDisclaimer)
        }
        items(SkillBridgeEcosystemData.walletTransactions) { tx ->
            val tone = when (tx.status) {
                "PAID" -> StatusTone.PAID
                "APPROVED" -> StatusTone.VERIFIED
                "UNDER_REVIEW" -> StatusTone.REVIEW
                "DISPUTED" -> StatusTone.DISPUTED
                else -> StatusTone.PENDING
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${tx.dateLabel} • +${tx.points} pts • ${tx.cashAmount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(tx.status, tone)
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
