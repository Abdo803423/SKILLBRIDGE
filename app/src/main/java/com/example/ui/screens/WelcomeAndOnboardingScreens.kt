package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.onGoogleSignInClicked
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.EvidenceProgressStepper
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.SkillBridgeEmblem
import com.example.ui.components.SkillBridgeLogo
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import kotlin.math.roundToInt

/**
 * Screen 1: Welcome & Auth Screen matching Phone #1 in SkillBridge Brand Showcase Poster.
 */
@Composable
fun WelcomeScreen(
    onToggleLanguage: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SkillBridgeColors.SoftCyanTint,
                        SkillBridgeColors.OffWhiteCanvas,
                        SkillBridgeColors.PureWhite
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkillBridgeLogo(darkBackground = false, compactOnly = false)
                OutlinedButton(
                    onClick = onToggleLanguage,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("welcome_language_toggle")
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.languageToggleLabel)
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SkillBridgeColors.PureWhite,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, SkillBridgeColors.BorderLight)
                ) {
                    Box(modifier = Modifier.padding(18.dp)) {
                        SkillBridgeEmblem(size = 76.dp, darkBackground = false)
                    }
                }

                Text(
                    text = strings.welcomeTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = SkillBridgeColors.DeepNavy
                )
                Text(
                    text = strings.welcomeSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = SkillBridgeColors.InkSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(SkillBridgeRadius.lg))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_bridge_1794605869240),
                        contentDescription = "SkillBridge career bridge visual",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        SkillBridgeColors.DeepNavy.copy(alpha = 0.1f),
                                        SkillBridgeColors.DeepNavy.copy(alpha = 0.72f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = SkillBridgeColors.LimeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (lang == AppLanguage.AR) "تحديات حقيقية • فرق • هندسة عكسية آمنة • سجل موثّق"
                            else "Challenges • Contests • Ethical Reverse Eng • Verified Passport",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkillBridgeColors.PureWhite
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.PureWhite),
                    border = BorderStroke(1.dp, SkillBridgeColors.BorderLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        EvidenceProgressStepper(activeStepIndex = 4)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!authError.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkillBridgeColors.DisputedRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        authError = null
                        isSigningIn = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                                onAuthSuccess()
                            },
                            onAuthError = { err ->
                                isSigningIn = false
                                authError = err
                            },
                            onAuthCancelled = {
                                isSigningIn = false
                            },
                            scope = scope
                        )
                    },
                    enabled = !isSigningIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkillBridgeColors.DeepNavy,
                        contentColor = SkillBridgeColors.PureWhite
                    ),
                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("welcome_get_started_button")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SkillBridgeColors.PureWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = strings.getStarted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }

                TextButton(
                    onClick = {
                        authError = null
                        isSigningIn = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                                onAuthSuccess()
                            },
                            onAuthError = { err ->
                                isSigningIn = false
                                authError = err
                            },
                            onAuthCancelled = { isSigningIn = false },
                            scope = scope
                        )
                    },
                    enabled = !isSigningIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("welcome_sign_in_button")
                ) {
                    Text(
                        text = strings.signInGoogle,
                        color = SkillBridgeColors.DeepNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Screen 2: Transparent 7-field Onboarding Wizard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardScreen(
    initialName: String,
    currentRoleMode: AppRoleMode,
    onSelectRoleMode: (AppRoleMode) -> Unit,
    onComplete: (
        displayName: String,
        country: String,
        timezone: String,
        domain: String,
        careerLevel: String,
        currentSkills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current

    var displayName by remember { mutableStateOf(initialName) }
    var country by remember { mutableStateOf("Egypt") }
    var timezone by remember { mutableStateOf("UTC+2") }
    var selectedDomain by remember { mutableStateOf(SkillBridgeEcosystemData.domains.first()) }
    var selectedLevel by remember { mutableStateOf("Mid-level") }
    var currentSkillsInput by remember { mutableStateOf("Kotlin, SQL, REST APIs") }
    var targetSkillsInput by remember { mutableStateOf("Distributed Systems, Compose Performance, Security Audits") }
    var goalsSummary by remember { mutableStateOf("Build verified backend & data evidence to transition into Senior engineering challenges.") }
    var weeklyHours by remember { mutableFloatStateOf(10f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (lang == AppLanguage.AR) "إعداد ملفك المهني (٧ خطوات سريعة)" else "Quick SkillBridge Setup (7 Transparent Steps)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        InfoNoteBanner(
            text = if (lang == AppLanguage.AR) {
                "نطلب هذه البيانات فقط لضبط توصيات التحديات والفرق ومنطقة التوقيت. مستوى الخبرة يتحدّث لاحقًا بالأدلة العملية الموثّقة."
            } else {
                "Why we ask this: to calibrate challenges, team timezones, and AI skill gaps. Your actual Career Level updates strictly from verified evidence."
            }
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text(if (lang == AppLanguage.AR) "١. الاسم المعروض" else "1. Display Name") },
            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text(if (lang == AppLanguage.AR) "٢. الدولة" else "2. Country") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = timezone,
                onValueChange = { timezone = it },
                label = { Text(if (lang == AppLanguage.AR) "التوقيت" else "Timezone") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = if (lang == AppLanguage.AR) "٣. الدور الحالي في المنصة" else "3. Active Role Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                AppRoleMode.LEARNER to strings.roleLearner,
                AppRoleMode.MENTOR to strings.roleMentor,
                AppRoleMode.DIRECTOR to strings.roleDirector,
                AppRoleMode.COMPANY to strings.roleCompany
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = currentRoleMode == mode,
                    onClick = { onSelectRoleMode(mode) },
                    label = { Text(label) }
                )
            }
        }

        Text(
            text = if (lang == AppLanguage.AR) "٤. المجال الأساسي" else "4. Primary Domain",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkillBridgeEcosystemData.domains.forEach { dom ->
                FilterChip(
                    selected = selectedDomain == dom,
                    onClick = { selectedDomain = dom },
                    label = { Text(dom) }
                )
            }
        }

        Text(
            text = if (lang == AppLanguage.AR) "٥. التقييم المبدئي للمستوى (يتعدل بالأدلة)" else "5. Initial Level Baseline (Evidence-Updated)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkillBridgeEcosystemData.careerLevels.forEach { lvl ->
                FilterChip(
                    selected = selectedLevel == lvl,
                    onClick = { selectedLevel = lvl },
                    label = { Text(lvl) }
                )
            }
        }

        OutlinedTextField(
            value = currentSkillsInput,
            onValueChange = { currentSkillsInput = it },
            label = { Text(if (lang == AppLanguage.AR) "٦. المهارات الحالية (مفصولة بفاصلة)" else "6. Current Skills (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = targetSkillsInput,
            onValueChange = { targetSkillsInput = it },
            label = { Text(if (lang == AppLanguage.AR) "المهارات المستهدفة" else "Target Skills") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = goalsSummary,
            onValueChange = { goalsSummary = it },
            label = { Text(if (lang == AppLanguage.AR) "٧. هدفك خلال ٩٠ يومًا" else "7. 90-Day Career Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = (if (lang == AppLanguage.AR) "الساعات الأسبوعية المتاحة: " else "Weekly Practice Hours: ") + "${weeklyHours.roundToInt()}h",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = weeklyHours,
            onValueChange = { weeklyHours = it },
            valueRange = 2f..30f
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text(if (lang == AppLanguage.AR) "تخطي" else "Back")
            }
            Button(
                onClick = {
                    val cur = currentSkillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val tgt = targetSkillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onComplete(
                        displayName.ifBlank { initialName },
                        country,
                        timezone,
                        selectedDomain,
                        selectedLevel,
                        cur,
                        tgt,
                        goalsSummary,
                        weeklyHours.roundToInt()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .testTag("onboarding_save_btn")
            ) {
                Text(if (lang == AppLanguage.AR) "حفظ والمتابعة" else "Save & Start Bridging")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
