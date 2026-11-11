package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import com.example.data.EmailDispatchResult
import com.example.data.GoogleAuthManager
import com.example.data.SmtpConfig
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.components.iosBounceClickable
import com.example.ui.theme.CaveatFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun rememberMonotonicMillis(resetKey: Any = Unit): Long {
    var frameMillis by remember(resetKey) { mutableLongStateOf(0L) }
    LaunchedEffect(resetKey) {
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) {
            frameMillis = withInfiniteAnimationFrameMillis { it - start }
        }
    }
    return frameMillis
}

private fun easeInOutCubic(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).let { it * it * it } / 2f
}

/**
 * Custom 6-Petal NoteVault Flower / Snowflake Emblem drawn with crisp vector geometry.
 * Matches the rotating white flower icon in the user's videos and screenshots.
 */
@Composable
fun NoteVaultFlowerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = Color(0xFFF5F6FA),
    rotationDegrees: Float = 0f,
    strokeWidthDp: Dp = 2.2.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val strokePx = strokeWidthDp.toPx()
        val petalLen = this.size.minDimension * 0.34f
        val petalWidth = this.size.minDimension * 0.16f

        rotate(degrees = rotationDegrees, pivot = Offset(cx, cy)) {
            for (i in 0 until 6) {
                rotate(degrees = i * 60f, pivot = Offset(cx, cy)) {
                    drawOval(
                        color = color,
                        topLeft = Offset(cx - petalWidth / 2f, cy - petalLen - strokePx * 0.4f),
                        size = Size(petalWidth, petalLen),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
            drawCircle(
                color = color,
                radius = strokePx * 0.9f,
                center = Offset(cx, cy)
            )
        }
    }
}

/**
 * Custom wireframe Globe icon matching the "Choose your language" screen in Video 1 (0:13).
 */
@Composable
fun NoteVaultGlobeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    color: Color = Color(0xFFF5F6FA)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (this.size.minDimension / 2f) * 0.86f
        val stroke = 1.8.dp.toPx()

        // Outer circle
        drawCircle(
            color = color,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = stroke)
        )
        // Inner vertical meridian oval
        drawOval(
            color = color,
            topLeft = Offset(cx - r * 0.48f, cy - r),
            size = Size(r * 0.96f, r * 2f),
            style = Stroke(width = stroke)
        )
        // Center vertical & horizontal lines
        drawLine(
            color = color,
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r),
            strokeWidth = stroke * 0.85f
        )
        drawLine(
            color = color,
            start = Offset(cx - r, cy),
            end = Offset(cx + r, cy),
            strokeWidth = stroke * 0.85f
        )
        // Latitude lines
        val latOffset = r * 0.48f
        val latHalfWidth = r * 0.86f
        drawLine(
            color = color,
            start = Offset(cx - latHalfWidth, cy - latOffset),
            end = Offset(cx + latHalfWidth, cy - latOffset),
            strokeWidth = stroke * 0.8f
        )
        drawLine(
            color = color,
            start = Offset(cx - latHalfWidth, cy + latOffset),
            end = Offset(cx + latHalfWidth, cy + latOffset),
            strokeWidth = stroke * 0.8f
        )
    }
}

/**
 * Entry screen replacing the old ball-menu WelcomeScreen:
 * 1. Plays the clean NoteVault-style Flower + "BushVPN" splash animation (no ball menus).
 * 2. If registration is not yet completed (or replayed), smoothly reveals the 5-step NoteVault
 *    onboarding flow: Language -> Email -> Name -> Username -> Password + Terms.
 * 3. If registration is already completed, automatically transitions into the main app after the splash!
 */
@Composable
fun WelcomeScreen(
    isEnglish: Boolean,
    isDarkTheme: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onStartClick: () -> Unit,
    isRegistrationCompleted: Boolean = false,
    initialEmail: String = "",
    initialName: String = "",
    initialUsername: String = "",
    latestVerificationCode: String? = null,
    emailDispatchResult: EmailDispatchResult? = null,
    smtpConfig: SmtpConfig = SmtpConfig(),
    onSaveSmtpConfig: (SmtpConfig) -> Unit = {},
    onSendEmailCode: (String) -> Unit = {},
    onVerifyEmailCode: (email: String, code: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, cb -> cb(true) },
    onCompleteRegistration: (email: String, name: String, username: String, selectedLangCode: String) -> Unit = { _, _, _, _ ->
        onStartClick()
    }
) {
    val colors = LocalAstralColors.current
    // 0 = Splash (0.0s - 1.8s), 1 = Welcome Screen ("Welcome" + "Get Started"), 2 = Registration Flow (Steps 1..5)
    var onboardingPhase by remember { mutableIntStateOf(0) }

    // Splash animation states (0.0s - 1.8s):
    // 0.0s - 0.5s: Opacity 0 -> 1, Scale 0.94 -> 1.0 (cubic-bezier(0.16, 1, 0.3, 1))
    // 0.5s - 1.3s: Static hold of logo, soft reveal of title
    // 1.3s - 1.8s: Fade out with gentle scale up (1.05)
    var splashPhase by remember { mutableIntStateOf(0) } // 0=entry, 1=hold, 2=exit

    val splashLogoScale by animateFloatAsState(
        targetValue = when (splashPhase) {
            0 -> 0.94f
            1 -> 1.0f
            else -> 1.05f
        },
        animationSpec = tween(
            durationMillis = if (splashPhase == 2) 480 else 500,
            easing = FastOutSlowInEasing
        ),
        label = "splash_logo_scale"
    )

    val splashLogoAlpha by animateFloatAsState(
        targetValue = when (splashPhase) {
            0 -> 0f
            1 -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (splashPhase == 2) 420 else 500,
            easing = FastOutSlowInEasing
        ),
        label = "splash_logo_alpha"
    )

    val splashTitleAlpha by animateFloatAsState(
        targetValue = if (splashPhase == 1) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "splash_title_alpha"
    )

    LaunchedEffect(Unit) {
        // 0.0s - 0.5s: Logo emerges
        delay(40)
        splashPhase = 1
        // 0.5s - 1.3s: Hold logo and title
        delay(800)
        // 1.3s - 1.8s: Soft exit
        splashPhase = 2
        delay(480)

        if (isRegistrationCompleted) {
            onStartClick()
        } else {
            onboardingPhase = 1 // Show Welcome Screen
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        AnimatedContent(
            targetState = onboardingPhase,
            transitionSpec = {
                (slideInHorizontally(tween(340, easing = FastOutSlowInEasing)) { full -> full / 2 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { full -> -full / 3 } + fadeOut(tween(220)))
            },
            label = "welcome_phase_transition"
        ) { phase ->
            when (phase) {
                0 -> {
                    // SPLASH SCREEN (0.0s – 1.8s) strictly on OLED pure black (#000000)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000))
                            .clickable {
                                if (isRegistrationCompleted) onStartClick() else onboardingPhase = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .scale(splashLogoScale)
                                .alpha(splashLogoAlpha)
                        ) {
                            NoteVaultFlowerIcon(
                                size = 68.dp,
                                color = Color(0xFFFFFFFF),
                                rotationDegrees = 0f,
                                strokeWidthDp = 2.4.dp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "BushVPN",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                letterSpacing = (-0.6).sp,
                                color = Color(0xFFFFFFFF),
                                modifier = Modifier.alpha(splashTitleAlpha)
                            )
                        }
                    }
                }

                1 -> {
                    // SCREEN 1: WELCOME SCREEN (Post-Installation Greeting)
                    // Full-screen vertical flex with 32px 24px padding (SpaceBetween)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000))
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Center: Large contour flower logo (~80x80px) + "Welcome" (28px bold) + subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NoteVaultFlowerIcon(
                                size = 80.dp,
                                color = Color(0xFFFFFFFF),
                                rotationDegrees = 0f,
                                strokeWidthDp = 2.6.dp
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = if (isEnglish) "Welcome" else "Добро пожаловать",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                letterSpacing = (-0.5).sp,
                                color = Color(0xFFFFFFFF),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isEnglish) {
                                    "Fast, minimal and private connection."
                                } else {
                                    "Быстрое, минималистичное и приватное соединение."
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center,
                                lineHeight = 21.sp
                            )
                        }

                        // Bottom: White button "Get Started" (height 52px, radius 16px) + Terms & Privacy (#48484A)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .iosBounceClickable { onboardingPhase = 2 }
                                    .testTag("welcome_get_started_button"),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFFFFF)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEnglish) "Get Started" else "Продолжить",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isEnglish) {
                                    "By continuing, you agree to our Terms and Privacy Policy"
                                } else {
                                    "Продолжая, вы принимаете Условия и Политику конфиденциальности"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF48484A),
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                2 -> {
                    // SCREEN 2: ONBOARDING / AUTH (Step-by-step)
                    NoteVaultRegistrationFlow(
                        initialIsEnglish = isEnglish,
                        initialEmail = initialEmail,
                        initialName = initialName,
                        initialUsername = initialUsername,
                        latestVerificationCode = latestVerificationCode,
                        emailDispatchResult = emailDispatchResult,
                        smtpConfig = smtpConfig,
                        onSaveSmtpConfig = onSaveSmtpConfig,
                        onSendEmailCode = onSendEmailCode,
                        onVerifyEmailCode = onVerifyEmailCode,
                        onSelectLanguage = { wantEnglish ->
                            if (wantEnglish != isEnglish) {
                                onToggleLanguage()
                            }
                        },
                        onFinish = { email, name, username, langCode ->
                            onCompleteRegistration(email, name, username, langCode)
                        }
                    )
                }
            }
        }
    }
}

private data class LanguageOption(
    val code: String,
    val label: String,
    val usesEnglishUi: Boolean
)

/**
 * NoteVault Onboarding & Registration Flow with Real Email 6-Digit Code Verification:
 * Step 0: Choose your language (Globe icon + Русский, Українська, English, Deutsch, Français)
 * Step 1: Enter your email (Flower + 5-step dash indicator "— · · · ·" + you@example.com + Send code + Google Sign-In)
 * Step 2: Enter 6-digit verification code ("· — · · ·" + 6-digit code sent to real email + Verify)
 * Step 3: What's your name? ("· · — · ·" + name input + Continue)
 * Step 4: Choose a username ("· · · — ·" + "@ username" input + Continue)
 * Step 5: Create a password ("· · · · —" + 2 password fields + Terms checkbox + red warning pill + Create account)
 */
@Composable
fun NoteVaultRegistrationFlow(
    initialIsEnglish: Boolean,
    initialEmail: String = "",
    initialName: String = "",
    initialUsername: String = "",
    @Suppress("UNUSED_PARAMETER") latestVerificationCode: String? = null,
    emailDispatchResult: EmailDispatchResult? = null,
    smtpConfig: SmtpConfig = SmtpConfig(),
    onSaveSmtpConfig: (SmtpConfig) -> Unit = {},
    onSendEmailCode: (String) -> Unit = {},
    onVerifyEmailCode: (email: String, code: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, cb -> cb(true) },
    onSelectLanguage: (Boolean) -> Unit,
    onFinish: (email: String, name: String, username: String, langCode: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = LocalAstralColors.current
    var showSmtpDialog by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            LanguageOption("ru", "Русский", false),
            LanguageOption("uk", "Українська", false),
            LanguageOption("en", "English", true),
            LanguageOption("de", "Deutsch", true),
            LanguageOption("fr", "Français", true)
        )
    }

    var currentStep by remember { mutableIntStateOf(0) } // 0=Lang, 1=Email, 2=Code, 3=Name, 4=Username, 5=Password
    var selectedLangCode by remember { mutableStateOf(if (initialIsEnglish) "en" else "ru") }
    val isEn = selectedLangCode != "ru" && selectedLangCode != "uk"

    var email by remember {
        mutableStateOf(if (initialEmail == "user@bushvpn.io") "" else initialEmail)
    }
    var enteredCode by remember { mutableStateOf("") }
    var showCodeError by remember { mutableStateOf(false) }
    var name by remember {
        mutableStateOf(if (initialName == "user") "" else initialName)
    }
    var username by remember {
        mutableStateOf(if (initialUsername == "bush_user") "" else initialUsername)
    }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var showTermsWarning by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "flower_idle_spin")
    val slowFlowerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowFlowerAngle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentStep == 0) {
            // STEP 0: Choose your language (Video 1 @ 0:13)
            Spacer(modifier = Modifier.height(42.dp))

            NoteVaultGlobeIcon(
                size = 58.dp,
                color = colors.primaryInk
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = if (isEn) "Choose your language" else "Выберите язык",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = colors.primaryInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.forEach { lang ->
                    val selected = selectedLangCode == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedLangCode = lang.code
                                onSelectLanguage(lang.usesEnglishUi)
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang.label,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp,
                            color = if (selected) colors.primaryInk else colors.secondaryText
                        )

                        // iOS Radio Circle
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.6.dp,
                                    color = if (selected) colors.primaryInk else colors.mutedText.copy(alpha = 0.6f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryInk)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { currentStep = 1 },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryInk,
                    contentColor = colors.onPrimaryInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_language_continue_button")
            ) {
                Text(
                    text = if (isEn) "Continue" else "Продолжить",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            // Top bar with Back Chevron `<` on the left
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            showTermsWarning = false
                            currentStep = (currentStep - 1).coerceAtLeast(0)
                        }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Top Centered Apple Flower Icon (36x36px) + 3-Step Dash Indicator (24x3px)
            NoteVaultFlowerIcon(
                size = 36.dp,
                color = Color(0xFFFFFFFF),
                rotationDegrees = slowFlowerAngle * 0.15f + (currentStep * 45f),
                strokeWidthDp = 2.0.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator: 3 thin horizontal bars (24x3px, active = #FFFFFF, inactive = rgba(255,255,255,0.16))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndicatorIndex = when {
                    currentStep <= 1 -> 0
                    currentStep == 2 -> 1
                    else -> 2
                }
                for (barIdx in 0..2) {
                    val isActive = activeIndicatorIndex == barIdx
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isActive) Color(0xFFFFFFFF) else Color(0x29FFFFFF)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Animated Step Body (Steps 1..5)
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState >= initialState
                    (slideInHorizontally(tween(260)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(180)))
                },
                label = "auth_step_content"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        1 -> {
                            // STEP 1: Enter your email + Send 6-Digit Code to Email & DB
                            Text(
                                text = if (isEn) "Enter your email" else "Введите вашу почту",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                color = colors.primaryInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) {
                                    "We'll send a 6-digit verification code to your email"
                                } else {
                                    "Мы отправим 6-значный код подтверждения на вашу почту"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(26.dp))

                            NoteVaultInputField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "you@example.com",
                                keyboardType = KeyboardType.Email,
                                testTag = "auth_email_input"
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            NoteVaultPrimaryButton(
                                text = if (isEn) "Send verification code" else "Отправить код на почту",
                                onClick = {
                                    val targetEmail = email.trim().ifBlank { "user@bushvpn.io" }
                                    email = targetEmail
                                    enteredCode = ""
                                    showCodeError = false
                                    onSendEmailCode(targetEmail)
                                    currentStep = 2
                                },
                                testTag = "auth_email_continue"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Google Sign-In via CredentialManager (Firebase Auth + Firestore sync)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                    .clickable {
                                        runCatching {
                                            val cm = CredentialManager.create(context)
                                            GoogleAuthManager.onGoogleSignInClicked(
                                                context = context,
                                                credentialManager = cm,
                                                onAuthSuccess = { gEmail, gName ->
                                                    if (!gEmail.isNullOrBlank()) email = gEmail
                                                    if (!gName.isNullOrBlank()) name = gName
                                                    val targetEmail = email.trim().ifBlank { "user@bushvpn.io" }
                                                    onSendEmailCode(targetEmail)
                                                    currentStep = 2
                                                },
                                                onAuthError = { err ->
                                                    googleSignInError = err
                                                },
                                                scope = scope
                                            )
                                        }.onFailure { err ->
                                            googleSignInError = err.localizedMessage ?: "Google Sign-In unavailable"
                                        }
                                    }
                                    .testTag("google_sign_in_button"),
                                shape = RoundedCornerShape(14.dp),
                                color = colors.card
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "G",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.primaryInk
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isEn) "Sign in with Google" else "Войти через Google",
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.primaryInk
                                    )
                                }
                            }

                            if (!googleSignInError.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = googleSignInError.orEmpty(),
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.5.sp,
                                    color = colors.secondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (smtpConfig.isConfigured) {
                                    if (isEn) "✓ Direct SMTP enabled (${smtpConfig.senderEmail})" else "✓ Прямой SMTP подключен (${smtpConfig.senderEmail})"
                                } else {
                                    if (isEn) "⚙️ Configure direct SMTP (Gmail / Mail.ru / Yandex)" else "⚙️ Настроить прямой SMTP (Gmail / Mail.ru / Yandex)"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = colors.secondaryText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSmtpDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        2 -> {
                            // STEP 2: 6-Digit Email Verification Code sent to real email
                            Text(
                                text = if (isEn) "Enter verification code" else "Введите код из письма",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                color = colors.primaryInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) {
                                    "We sent a 6-digit code to $email"
                                } else {
                                    "Письмо с 6-значным кодом отправлено на $email"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Real Email Delivery Status Card (No fake phone notification / no code leak)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                color = colors.card
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    val statusText = when {
                                        emailDispatchResult == null -> if (isEn) {
                                            "Connecting to mail server and dispatching email to $email..."
                                        } else {
                                            "Отправляем электронное письмо на $email..."
                                        }
                                        isEn -> emailDispatchResult.statusMessageEn
                                        else -> emailDispatchResult.statusMessageRu
                                    }
                                    Text(
                                        text = statusText,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = if (emailDispatchResult?.delivered == true) {
                                            Color(0xFF34D399)
                                        } else {
                                            colors.secondaryText
                                        },
                                        lineHeight = 17.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 6-Digit iOS NoteVault Code Input Field + Visual 6-Box Grid
                            NoteVaultSixDigitCodeField(
                                code = enteredCode,
                                onCodeChange = { next ->
                                    enteredCode = next.filter { it.isDigit() }.take(6)
                                    showCodeError = false
                                },
                                testTag = "auth_verification_code_input"
                            )

                            AnimatedVisibility(visible = showCodeError) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF2B1215),
                                        modifier = Modifier.border(
                                            1.dp,
                                            Color(0xFFEF4444).copy(alpha = 0.45f),
                                            RoundedCornerShape(50)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) {
                                                    "Invalid 6-digit code. Check your email inbox."
                                                } else {
                                                    "Неверный 6-значный код. Проверьте почту (Входящие / Спам)."
                                                },
                                                fontFamily = PlusJakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            NoteVaultPrimaryButton(
                                text = if (isEn) "Verify code" else "Подтвердить код",
                                onClick = {
                                    val candidate = enteredCode.trim()
                                    if (candidate.length < 6) {
                                        showCodeError = true
                                    } else {
                                        onVerifyEmailCode(email, candidate) { valid ->
                                            if (valid) {
                                                showCodeError = false
                                                currentStep = 3
                                            } else {
                                                showCodeError = true
                                            }
                                        }
                                    }
                                },
                                testTag = "auth_verify_code_continue"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isEn) "Resend code to email" else "Отправить письмо с кодом повторно",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.primaryInk,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        enteredCode = ""
                                        showCodeError = false
                                        onSendEmailCode(email)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isEn) {
                                    "⚙️ Configure direct SMTP (Gmail / Mail.ru / Yandex)"
                                } else {
                                    "⚙️ Настроить прямую отправку через SMTP (Gmail / Mail.ru)"
                                },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = colors.secondaryText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSmtpDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        3 -> {
                            // STEP 3: What's your name? (Video 1 @ 0:08)
                            Text(
                                text = if (isEn) "What's your name?" else "Как вас зовут?",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                color = colors.primaryInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) "This name will be visible to others" else "Это имя будет отображаться в профиле",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(26.dp))

                            NoteVaultInputField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = if (isEn) "Your name" else "Ваше имя",
                                testTag = "auth_name_input"
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            NoteVaultPrimaryButton(
                                text = if (isEn) "Continue" else "Продолжить",
                                onClick = {
                                    if (name.isBlank()) name = "Operator"
                                    currentStep = 4
                                },
                                testTag = "auth_name_continue"
                            )
                        }

                        4 -> {
                            // STEP 4: Choose a username (Video 1 @ 0:10 & Video 2 @ 0:00)
                            Text(
                                text = if (isEn) "Choose a username" else "Выберите юзернейм",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                color = colors.primaryInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) "5–24 characters: letters, numbers, _" else "5–24 символа: буквы, цифры, _",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(26.dp))

                            NoteVaultInputField(
                                value = username,
                                onValueChange = { raw ->
                                    username = raw.removePrefix("@").lowercase()
                                },
                                placeholder = "username",
                                prefix = "@",
                                testTag = "auth_username_input"
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            NoteVaultPrimaryButton(
                                text = if (isEn) "Continue" else "Продолжить",
                                onClick = {
                                    if (username.isBlank()) {
                                        username = name.lowercase().replace(" ", "_").ifBlank { "bush_user" }
                                    }
                                    currentStep = 5
                                },
                                testTag = "auth_username_continue"
                            )
                        }

                        else -> {
                            // STEP 5: Create a password + Terms checkbox + Red warning pill (Video 2 @ 0:02–0:07)
                            Text(
                                text = if (isEn) "Create a password" else "Создайте пароль",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                color = colors.primaryInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) "At least 8 characters" else "Минимум 8 символов",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            NoteVaultInputField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = if (isEn) "Password" else "Пароль",
                                isPassword = true,
                                testTag = "auth_password_input"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            NoteVaultInputField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = if (isEn) "Confirm password" else "Повторите пароль",
                                isPassword = true,
                                testTag = "auth_confirm_password_input"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Terms of Service & Privacy Policy circular checkbox row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        agreedToTerms = !agreedToTerms
                                        if (agreedToTerms) showTermsWarning = false
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                                    .testTag("auth_terms_checkbox"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (agreedToTerms) colors.primaryInk else Color.Transparent)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (agreedToTerms) colors.primaryInk else colors.mutedText,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (agreedToTerms) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.onPrimaryInk,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isEn) {
                                        "I agree to the Terms of Service and Privacy Policy"
                                    } else {
                                        "Я принимаю условия сервиса и политику конфиденциальности"
                                    },
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp,
                                    color = colors.secondaryText
                                )
                            }

                            // Red Warning Pill if user clicks "Create account" before checking Terms (Video 2 @ 0:04)
                            AnimatedVisibility(visible = showTermsWarning) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF2B1215),
                                        modifier = Modifier.border(
                                            1.dp,
                                            Color(0xFFEF4444).copy(alpha = 0.45f),
                                            RoundedCornerShape(50)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) {
                                                    "Accept Terms and Privacy Policy"
                                                } else {
                                                    "Примите условия и политику конфиденциальности"
                                                },
                                                fontFamily = PlusJakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            NoteVaultPrimaryButton(
                                text = if (isEn) "Create account" else "Создать аккаунт",
                                onClick = {
                                    if (!agreedToTerms) {
                                        showTermsWarning = true
                                    } else {
                                        onFinish(
                                            email.ifBlank { "user@bushvpn.io" },
                                            name.ifBlank { "Operator" },
                                            username.ifBlank { "bush_user" },
                                            selectedLangCode
                                        )
                                    }
                                },
                                testTag = "auth_create_account_button"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSmtpDialog) {
        NoteVaultSmtpConfigDialog(
            initialConfig = smtpConfig,
            isEn = isEn,
            onDismiss = { showSmtpDialog = false },
            onSave = { updated ->
                onSaveSmtpConfig(updated)
                showSmtpDialog = false
                if (currentStep == 2 && email.isNotBlank()) {
                    onSendEmailCode(email)
                }
            }
        )
    }
}

@Composable
private fun NoteVaultSmtpConfigDialog(
    initialConfig: SmtpConfig,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSave: (SmtpConfig) -> Unit
) {
    val colors = LocalAstralColors.current
    var host by remember { mutableStateOf(initialConfig.host.ifBlank { "smtp.gmail.com" }) }
    var portStr by remember { mutableStateOf(initialConfig.port.toString()) }
    var senderEmail by remember { mutableStateOf(initialConfig.senderEmail) }
    var appPassword by remember { mutableStateOf(initialConfig.appPassword) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = colors.cardElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEn) "Direct SMTP Mail Server" else "Прямая отправка через SMTP",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = colors.primaryInk
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "Use your Gmail / Mail.ru / Yandex account + 16-char App Password to send real 6-digit verification emails directly via STARTTLS."
                    } else {
                        "Укажите вашу почту отправителя (Gmail / Mail.ru / Yandex) и 16-значный «Пароль приложения» для прямой отправки писем с кодом."
                    },
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 12.sp,
                    color = colors.secondaryText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(2f)) {
                        NoteVaultInputField(
                            value = host,
                            onValueChange = { host = it },
                            placeholder = "smtp.gmail.com",
                            testTag = "smtp_host_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        NoteVaultInputField(
                            value = portStr,
                            onValueChange = { portStr = it.filter { ch -> ch.isDigit() }.take(5) },
                            placeholder = "587",
                            keyboardType = KeyboardType.Number,
                            testTag = "smtp_port_input"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                NoteVaultInputField(
                    value = senderEmail,
                    onValueChange = { senderEmail = it },
                    placeholder = if (isEn) "Sender email (e.g. you@gmail.com)" else "Почта отправителя (you@gmail.com)",
                    keyboardType = KeyboardType.Email,
                    testTag = "smtp_sender_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                NoteVaultInputField(
                    value = appPassword,
                    onValueChange = { appPassword = it },
                    placeholder = if (isEn) "16-char App Password" else "Пароль приложения (16 символов)",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    testTag = "smtp_password_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                NoteVaultPrimaryButton(
                    text = if (isEn) "Save & Send via SMTP" else "Сохранить и отправлять через SMTP",
                    onClick = {
                        onSave(
                            SmtpConfig(
                                host = host.trim().ifBlank { "smtp.gmail.com" },
                                port = portStr.toIntOrNull() ?: 587,
                                senderEmail = senderEmail.trim(),
                                appPassword = appPassword.trim()
                            )
                        )
                    },
                    testTag = "smtp_save_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isEn) "Cancel" else "Отмена",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NoteVaultInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    prefix: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    val colors = LocalAstralColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.2.dp, colors.border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = colors.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 15.sp,
                        color = colors.mutedText
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = colors.primaryInk
                    ),
                    cursorBrush = SolidColor(colors.primaryInk),
                    visualTransformation = if (isPassword) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
            }
        }
    }
}

@Composable
private fun NoteVaultSixDigitCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    testTag: String
) {
    val colors = LocalAstralColors.current
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = code,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(6)
                onCodeChange(digits)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            cursorBrush = SolidColor(colors.primaryInk),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (idx in 0 until 6) {
                        val char = code.getOrNull(idx)?.toString() ?: ""
                        val isFocusedSlot = code.length == idx || (code.length == 6 && idx == 5)
                        val borderColor by animateColorAsState(
                            targetValue = when {
                                char.isNotEmpty() -> colors.primaryInk.copy(alpha = 0.85f)
                                isFocusedSlot -> colors.primaryInk.copy(alpha = 0.55f)
                                else -> colors.border
                            },
                            animationSpec = tween(180),
                            label = "otp_slot_border_$idx"
                        )
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(
                                    width = if (isFocusedSlot || char.isNotEmpty()) 1.5.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.card
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = colors.primaryInk
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun NoteVaultPrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .iosBounceClickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFFFF)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF000000)
            )
        }
    }
}

@Composable
fun AdvantagesScreen(
    isEnglish: Boolean,
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        onContinue()
    }
    ConnectingWaveScreen(isEnglish = isEnglish)
}

/**
 * 60fps Motion-Captured 2D Rigid-Body Gravity & Collision Trajectory from main_9695194f.mp4
 * kept for the offline "No internet connection" overlay.
 */
private val SIX_BALL_PHYSICS_FRAMES: Array<FloatArray> = arrayOf(
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.499f, 0.865f, 0.499f, 0.000f, 0.998f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.498f, 0.866f, 0.498f, 0.000f, 0.998f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.499f, 0.866f, 0.500f, 0.000f, 0.998f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.498f, 0.865f, 0.509f, 0.000f, 0.998f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.494f, 0.865f, 0.525f, 0.000f, 0.998f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.487f, 0.865f, 0.546f, 0.000f, 0.999f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.478f, 0.866f, 0.576f, 0.000f, 1.002f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.466f, 0.865f, 0.611f, 0.000f, 1.005f, -0.865f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.451f, 0.865f, 0.659f, 0.000f, 1.014f, -0.866f, 0.498f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.865f, -0.434f, 0.866f, 0.715f, 0.001f, 1.027f, -0.866f, 0.499f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.414f, 0.865f, 0.785f, 0.000f, 1.045f, -0.866f, 0.500f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.865f, -0.389f, 0.865f, 0.872f, 0.000f, 1.071f, -0.866f, 0.501f),
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.001f, 0.865f, -0.361f, 0.865f, 0.971f, 0.000f, 1.100f, -0.865f, 0.503f),
    floatArrayOf(-0.866f, -0.498f, 0.000f, -0.999f, 0.865f, -0.328f, 0.866f, 1.096f, 0.000f, 1.136f, -0.867f, 0.509f),
    floatArrayOf(-0.867f, -0.494f, 0.000f, -0.995f, 0.866f, -0.289f, 0.866f, 1.257f, -0.001f, 1.182f, -0.865f, 0.517f),
    floatArrayOf(-0.866f, -0.489f, 0.000f, -0.992f, 0.866f, -0.242f, 0.866f, 1.379f, -0.001f, 1.238f, -0.866f, 0.529f),
    floatArrayOf(-0.866f, -0.482f, 0.000f, -0.983f, 0.866f, -0.187f, 0.866f, 1.326f, 0.000f, 1.317f, -0.866f, 0.545f),
    floatArrayOf(-0.866f, -0.472f, 0.000f, -0.969f, 0.866f, -0.120f, 0.866f, 1.295f, 0.000f, 1.397f, -0.866f, 0.569f),
    floatArrayOf(-0.866f, -0.462f, 0.000f, -0.946f, 0.866f, -0.040f, 0.865f, 1.278f, 0.000f, 1.369f, -0.866f, 0.604f),
    floatArrayOf(-0.866f, -0.448f, 0.001f, -0.912f, 0.866f, 0.066f, 0.866f, 1.272f, -0.001f, 1.354f, -0.866f, 0.649f),
    floatArrayOf(-0.866f, -0.434f, 0.000f, -0.861f, 0.866f, 0.206f, 0.865f, 1.277f, -0.001f, 1.349f, -0.865f, 0.708f),
    floatArrayOf(-0.866f, -0.414f, 0.001f, -0.786f, 0.866f, 0.505f, 0.866f, 1.293f, 0.000f, 1.347f, -0.865f, 0.791f),
    floatArrayOf(-0.866f, -0.393f, 0.000f, -0.690f, 0.905f, 0.494f, 0.865f, 1.319f, 0.000f, 1.353f, -0.865f, 0.895f),
    floatArrayOf(-0.866f, -0.368f, 0.001f, -0.552f, 1.000f, 0.467f, 0.866f, 1.361f, 0.000f, 1.364f, -0.865f, 1.033f),
    floatArrayOf(-0.866f, -0.340f, 0.001f, -0.361f, 1.134f, 0.454f, 0.866f, 1.396f, 0.000f, 1.384f, -0.865f, 1.216f),
    floatArrayOf(-0.866f, -0.308f, 0.000f, -0.108f, 1.287f, 0.478f, 0.865f, 1.384f, 0.000f, 1.401f, -0.871f, 1.404f),
    floatArrayOf(-0.865f, -0.271f, 0.002f, 0.233f, 1.445f, 0.565f, 0.866f, 1.382f, 0.000f, 1.395f, -0.940f, 1.375f),
    floatArrayOf(-0.866f, -0.229f, 0.008f, 0.621f, 1.590f, 0.739f, 0.866f, 1.387f, 0.000f, 1.396f, -0.993f, 1.367f),
    floatArrayOf(-0.866f, -0.180f, 0.114f, 0.609f, 1.708f, 1.031f, 0.866f, 1.404f, 0.000f, 1.402f, -1.030f, 1.374f),
    floatArrayOf(-0.866f, -0.124f, 0.214f, 0.603f, 1.786f, 1.389f, 0.866f, 1.401f, 0.000f, 1.405f, -1.053f, 1.388f),
    floatArrayOf(-0.865f, -0.060f, 0.304f, 0.614f, 1.884f, 1.361f, 0.866f, 1.401f, 0.000f, 1.405f, -1.063f, 1.401f),
    floatArrayOf(-0.865f, 0.015f, 0.381f, 0.636f, 1.961f, 1.351f, 0.866f, 1.404f, 0.000f, 1.405f, -1.079f, 1.406f),
    floatArrayOf(-0.866f, 0.117f, 0.453f, 0.665f, 2.032f, 1.351f, 0.867f, 1.405f, 0.000f, 1.405f, -1.128f, 1.406f),
    floatArrayOf(-0.865f, 0.225f, 0.497f, 0.687f, 2.086f, 1.355f, 0.866f, 1.405f, 0.000f, 1.406f, -1.152f, 1.406f),
    floatArrayOf(-0.865f, 0.388f, 0.527f, 0.703f, 2.133f, 1.357f, 0.866f, 1.407f, 0.000f, 1.406f, -1.171f, 1.406f),
    floatArrayOf(-0.865f, 0.708f, 0.534f, 0.709f, 2.176f, 1.362f, 0.867f, 1.407f, 0.000f, 1.406f, -1.185f, 1.407f),
    floatArrayOf(-0.838f, 0.714f, 0.485f, 0.714f, 2.212f, 1.366f, 0.866f, 1.406f, 0.000f, 1.406f, -1.199f, 1.407f),
    floatArrayOf(-0.773f, 0.713f, 0.436f, 0.715f, 2.245f, 1.372f, 0.866f, 1.406f, 0.000f, 1.405f, -1.209f, 1.406f),
    floatArrayOf(-0.694f, 0.727f, 0.388f, 0.719f, 2.273f, 1.375f, 0.866f, 1.406f, 0.000f, 1.406f, -1.218f, 1.406f),
    floatArrayOf(-0.619f, 0.761f, 0.347f, 0.723f, 2.297f, 1.379f, 0.866f, 1.406f, -0.001f, 1.408f, -1.226f, 1.406f),
    floatArrayOf(-0.573f, 0.823f, 0.379f, 0.739f, 2.319f, 1.384f, 0.867f, 1.407f, 0.000f, 1.407f, -1.234f, 1.406f),
    floatArrayOf(-0.576f, 0.900f, 0.402f, 0.752f, 2.337f, 1.388f, 0.866f, 1.406f, 0.001f, 1.408f, -1.240f, 1.406f),
    floatArrayOf(-0.580f, 0.903f, 0.420f, 0.761f, 2.352f, 1.389f, 0.866f, 1.406f, 0.001f, 1.407f, -1.245f, 1.406f),
    floatArrayOf(-0.588f, 0.910f, 0.432f, 0.768f, 2.365f, 1.391f, 0.867f, 1.407f, 0.000f, 1.407f, -1.250f, 1.406f),
    floatArrayOf(-0.598f, 0.921f, 0.432f, 0.767f, 2.377f, 1.395f, 0.867f, 1.407f, 0.000f, 1.407f, -1.253f, 1.406f),
    floatArrayOf(-0.609f, 0.934f, 0.432f, 0.768f, 2.388f, 1.395f, 0.867f, 1.407f, 0.000f, 1.407f, -1.257f, 1.406f),
    floatArrayOf(-0.618f, 0.947f, 0.432f, 0.768f, 2.397f, 1.396f, 0.867f, 1.407f, 0.000f, 1.407f, -1.259f, 1.406f),
    floatArrayOf(-0.628f, 0.959f, 0.432f, 0.768f, 2.404f, 1.398f, 0.867f, 1.407f, 0.001f, 1.407f, -1.261f, 1.406f),
    floatArrayOf(-0.633f, 0.966f, 0.432f, 0.768f, 2.411f, 1.400f, 0.867f, 1.407f, 0.000f, 1.407f, -1.266f, 1.406f),
    floatArrayOf(-0.634f, 0.970f, 0.432f, 0.768f, 2.415f, 1.400f, 0.867f, 1.407f, 0.000f, 1.407f, -1.267f, 1.406f)
)

@Composable
fun NoInternetSixDotsAnimation(
    dotColor: Color,
    triggerKey: Int = 0,
    pauseBetweenCyclesMs: Long = 3800L,
    modifier: Modifier = Modifier
) {
    val elapsedMs = rememberMonotonicMillis(resetKey = triggerKey)
    val initialSpinMs = 260L
    val fallDurationMs = 1550L
    val riseDurationMs = 1450L
    val loopSpinMs = 950L
    val totalCycleMs = initialSpinMs + fallDurationMs + pauseBetweenCyclesMs + riseDurationMs + loopSpinMs
    val tMs = elapsedMs % totalCycleMs

    val maxIndex = (SIX_BALL_PHYSICS_FRAMES.size - 1).toFloat()

    val exactFramePos: Float = when {
        tMs < initialSpinMs -> 0f
        tMs < initialSpinMs + fallDurationMs -> {
            val fallElapsed = (tMs - initialSpinMs).toFloat()
            val p = (fallElapsed / fallDurationMs.toFloat()).coerceIn(0f, 1f)
            p * maxIndex
        }
        tMs < initialSpinMs + fallDurationMs + pauseBetweenCyclesMs -> maxIndex
        tMs < initialSpinMs + fallDurationMs + pauseBetweenCyclesMs + riseDurationMs -> {
            val riseElapsed = (tMs - (initialSpinMs + fallDurationMs + pauseBetweenCyclesMs)).toFloat()
            val p = easeInOutCubic((riseElapsed / riseDurationMs.toFloat()).coerceIn(0f, 1f))
            (1f - p) * maxIndex
        }
        else -> 0f
    }

    val spinAngleRad: Float = when {
        tMs < initialSpinMs -> {
            val p = easeInOutCubic(tMs.toFloat() / initialSpinMs.toFloat())
            p * (PI.toFloat() / 3f)
        }
        tMs >= initialSpinMs + fallDurationMs + pauseBetweenCyclesMs + riseDurationMs -> {
            val spinElapsed = (tMs - (initialSpinMs + fallDurationMs + pauseBetweenCyclesMs + riseDurationMs)).toFloat()
            val p = easeInOutCubic((spinElapsed / loopSpinMs.toFloat()).coerceIn(0f, 1f))
            p * (2f * PI.toFloat() / 3f)
        }
        else -> 0f
    }

    val circleSpinWeight: Float = (1f - (exactFramePos / 12f)).coerceIn(0f, 1f)

    val idx0 = exactFramePos.toInt().coerceIn(0, SIX_BALL_PHYSICS_FRAMES.lastIndex)
    val idx1 = (idx0 + 1).coerceAtMost(SIX_BALL_PHYSICS_FRAMES.lastIndex)
    val frac = (exactFramePos - idx0).coerceIn(0f, 1f)

    val frameA = SIX_BALL_PHYSICS_FRAMES[idx0]
    val frameB = SIX_BALL_PHYSICS_FRAMES[idx1]

    Canvas(
        modifier = modifier
            .width(184.dp)
            .height(92.dp)
    ) {
        val ringRadiusPx = 19.5.dp.toPx()
        val dotRadiusPx = ringRadiusPx * 0.388f
        val cx = size.width / 2f - ringRadiusPx * 0.20f
        val cy = size.height / 2f - ringRadiusPx * 0.22f

        val effAngle = spinAngleRad * circleSpinWeight
        val cosA = cos(effAngle)
        val sinA = sin(effAngle)

        for (i in 0 until 6) {
            val rawX = frameA[i * 2] + (frameB[i * 2] - frameA[i * 2]) * frac
            val rawY = frameA[i * 2 + 1] + (frameB[i * 2 + 1] - frameA[i * 2 + 1]) * frac

            val nx = if (circleSpinWeight > 0.001f) {
                rawX * cosA - rawY * sinA
            } else {
                rawX
            }
            val ny = if (circleSpinWeight > 0.001f) {
                rawX * sinA + rawY * cosA
            } else {
                rawY
            }

            drawCircle(
                color = dotColor,
                radius = dotRadiusPx,
                center = Offset(
                    x = cx + nx * ringRadiusPx,
                    y = cy + ny * ringRadiusPx
                )
            )
        }
    }
}

@Composable
fun ConnectingWaveScreen(
    @Suppress("UNUSED_PARAMETER") isEnglish: Boolean
) {
    val colors = LocalAstralColors.current
    val elapsedMs = rememberMonotonicMillis()
    val rot = (elapsedMs / 8f) % 360f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NoteVaultFlowerIcon(
                size = 52.dp,
                color = colors.primaryInk,
                rotationDegrees = rot,
                strokeWidthDp = 2.3.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "BushVPN",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = colors.primaryInk
            )
        }
    }
}

@Composable
fun NoInternetConnectionOverlay(
    @Suppress("UNUSED_PARAMETER") isEnglish: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val colors = LocalAstralColors.current
    var replayTrigger by remember { mutableIntStateOf(0) }
    val backdropInteraction = remember { MutableInteractionSource() }
    val contentInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable(
                interactionSource = backdropInteraction,
                indication = null,
                onClick = onDismiss
            )
            .padding(28.dp)
            .testTag("no_internet_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = contentInteraction,
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 16.dp)
        ) {
            NoInternetSixDotsAnimation(
                dotColor = colors.primaryInk,
                triggerKey = replayTrigger,
                pauseBetweenCyclesMs = 3800L
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No internet connection",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.primaryInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reconnect to a stable network to continue",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    replayTrigger++
                    onRetry()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryInk,
                    contentColor = colors.onPrimaryInk
                ),
                modifier = Modifier
                    .height(46.dp)
                    .width(136.dp)
                    .testTag("no_internet_try_again_button")
            ) {
                Text(
                    text = "Try again",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
