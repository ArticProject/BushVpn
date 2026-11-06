package com.example.ui.screens

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
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

private fun easeOutCubic(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val inv = 1f - x
    return 1f - inv * inv * inv
}

private fun easeOutBack(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val p = x - 1f
    return (1f + c3 * p * p * p + c1 * p * p).coerceAtLeast(0f)
}

@Composable
fun WelcomeScreen(
    isEnglish: Boolean,
    isDarkTheme: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onStartClick: () -> Unit
) {
    val colors = LocalAstralColors.current
    val elapsedMs = rememberMonotonicMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: ASTRAL ∞ + Theme & RU/EN Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ASTRAL",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp,
                    color = colors.primaryInk
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "∞",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = colors.primaryInk
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.pillBackground)
                        .border(1.dp, colors.border, CircleShape)
                        .testTag("welcome_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = colors.primaryInk,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.pillBackground)
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .clickable(onClick = onToggleLanguage)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("welcome_lang_toggle")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RU",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = if (!isEnglish) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (!isEnglish) colors.primaryInk else colors.mutedText
                        )
                        Text(
                            text = " / ",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                        Text(
                            text = "EN",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = if (isEnglish) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isEnglish) colors.primaryInk else colors.mutedText
                        )
                    }
                }
            }
        }

        // Center Hero Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "WELCOME",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
                letterSpacing = 1.5.sp,
                color = colors.primaryInk,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Handcrafted digital aesthetics with sovereign cryptography.\nSilent routing of personal traffic through isolated enclaves.",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = colors.secondaryText,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Smooth Rotating Circle Arc around START Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val buttonCenterX = size.width / 2f
                val buttonCenterY = size.height - 46.dp.toPx()
                val radiusX = 136.dp.toPx()
                val radiusY = 82.dp.toPx()
                val dotRadius = 21.dp.toPx()

                val totalDots = 10
                val continuousPhase = (elapsedMs.toDouble() / 9000.0) * (2.0 * PI)

                for (i in 0 until totalDots) {
                    val angle = ((i.toDouble() / totalDots) * 2.0 * PI + continuousPhase) % (2.0 * PI)
                    val x = buttonCenterX + cos(angle).toFloat() * radiusX
                    val y = buttonCenterY + sin(angle).toFloat() * radiusY

                    val topWeight = ((-sin(angle).toFloat() + 0.35f) / 1.35f).coerceIn(0f, 1f)
                    val alpha = (0.10f + 0.90f * easeInOutCubic(topWeight)).coerceIn(0.10f, 1f)
                    val scale = 0.86f + 0.14f * topWeight

                    drawCircle(
                        color = colors.primaryInk.copy(alpha = alpha),
                        radius = dotRadius * scale,
                        center = Offset(x, y)
                    )
                }
            }

            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryInk,
                    contentColor = colors.onPrimaryInk
                ),
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .width(164.dp)
                    .height(54.dp)
                    .testTag("welcome_start_button")
            ) {
                Text(
                    text = "START",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
fun AdvantagesScreen(
    isEnglish: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = LocalAstralColors.current
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    val elapsedMs = rememberMonotonicMillis()
    val cycleDuration = 6400L
    val tMs = (elapsedMs % cycleDuration).toFloat()

    val orbitRotationRad: Float = when {
        tMs < 1100f -> 0f
        tMs < 3600f -> {
            val p = easeInOutCubic((tMs - 1100f) / 2500f)
            p * (PI.toFloat() * 1.25f)
        }
        tMs < 5400f -> {
            val base = PI.toFloat() * 1.25f
            val p = (tMs - 3600f) / 1800f
            base + p * (PI.toFloat() * 1.35f)
        }
        else -> 0f
    }

    val radiusDpValue: Float = when {
        tMs < 3600f -> 66f
        tMs < 4400f -> {
            val p = easeInOutCubic((tMs - 3600f) / 800f)
            66f - p * 51f
        }
        tMs < 5000f -> 15f
        tMs < 5450f -> {
            val p = easeInOutCubic((tMs - 5000f) / 450f)
            15f * (1f - p)
        }
        else -> 0f
    }

    val circleSizeDpValue: Float = when {
        tMs < 3600f -> 50f
        tMs < 4400f -> {
            val p = easeInOutCubic((tMs - 3600f) / 800f)
            50f - p * 41f
        }
        tMs < 5450f -> 9f
        else -> {
            val p = ((tMs - 5450f) / 950f).coerceIn(0f, 1f)
            10f * (1f - easeInOutCubic(p))
        }
    }

    val iconAlpha: Float = when {
        tMs < 3500f -> 1f
        tMs < 3950f -> (1f - (tMs - 3500f) / 450f).coerceIn(0f, 1f)
        else -> 0f
    }

    val advantageIcons = remember {
        listOf(
            Icons.Default.Person,
            Icons.Default.Key,
            Icons.Default.Dns,
            Icons.Default.Speed,
            Icons.Default.Shield,
            Icons.Default.Bolt
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, colors.border, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = colors.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.card)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("advantages_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Advantages",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.primaryInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 40.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    advantageIcons.forEachIndexed { index, icon ->
                        val staggerStart = index * 135f
                        val popScale = if (tMs < 1100f) {
                            val localT = ((tMs - staggerStart) / 380f).coerceIn(0f, 1f)
                            easeOutBack(localT)
                        } else {
                            1f
                        }

                        if (popScale > 0.02f && circleSizeDpValue > 0.5f) {
                            val baseAngle = -PI.toFloat() / 2f + index * (2f * PI.toFloat() / 6f)
                            val currentAngle = baseAngle + orbitRotationRad
                            val offsetX = (cos(currentAngle) * radiusDpValue).dp
                            val offsetY = (sin(currentAngle) * radiusDpValue).dp
                            val effectiveDiameter = (circleSizeDpValue * popScale).dp

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = offsetX.roundToPx(),
                                            y = offsetY.roundToPx()
                                        )
                                    }
                                    .size(effectiveDiameter)
                                    .clip(CircleShape)
                                    .background(colors.primaryInk),
                                contentAlignment = Alignment.Center
                            ) {
                                if (iconAlpha > 0.05f) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = colors.onPrimaryInk,
                                        modifier = Modifier
                                            .size((20f * popScale).dp)
                                            .alpha(iconAlpha)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sovereign X-Ray Core",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = colors.primaryInk,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Next-gen VLESS REALITY protocol, instant Android shade quick-toggle, sovereign profiles, and hardware-grade zero logging.",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            val isReady = countdown <= 0
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) colors.primaryInk else colors.pillBackground,
                    contentColor = if (isReady) colors.onPrimaryInk else colors.secondaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("advantages_continue_button")
            ) {
                Text(
                    text = if (countdown > 0) "Continue ($countdown)" else "Continue",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Exact 6-Dot No-Internet Animation from main_9695194f.mp4:
 * - Starts IMMEDIATELY at t=0ms when the overlay appears (no initial wait).
 * - 0..380ms: Uncoils into the organic 5-dot left wave cluster + 1 detached right satellite dot (frames 00:00, 00:02, 00:03).
 * - 380..1550ms: Liquid wave pulse travels through the 5-dot cluster while the 6th dot hovers on the right.
 * - 1550..2150ms: The detached right dot snaps back in and all 6 dots curl into the 6-dot flower/ring (frames 00:01, 00:04).
 * - 2150..2550ms: The 6-dot ring rotates and settles smoothly.
 * - 2550..7550ms: Rests for exactly 5 seconds (5000ms) before starting the next cycle!
 */
@Composable
fun NoInternetSixDotsAnimation(
    dotColor: Color,
    triggerKey: Int = 0,
    pauseBetweenCyclesMs: Long = 5000L,
    modifier: Modifier = Modifier
) {
    val elapsedMs = rememberMonotonicMillis(resetKey = triggerKey)
    val activeAnimMs = 2550L
    val totalCycleMs = activeAnimMs + pauseBetweenCyclesMs
    val tMs = (elapsedMs % totalCycleMs).toFloat()

    // Horizontal cluster positions (in dp relative to center) matching frames 00:00, 00:02, 00:03:
    // 5 dots grouped closely on the left in a slight arch, and the 6th dot detached to the right!
    val clusterX = remember { floatArrayOf(-24f, -15.5f, -6.5f, 2.5f, 10.5f, 26.5f) }
    val clusterBaseY = remember { floatArrayOf(2.5f, -1.5f, -3.5f, 1.8f, 0.5f, 1.2f) }
    val clusterBaseR = remember { floatArrayOf(4.2f, 4.8f, 6.8f, 6.2f, 4.5f, 3.8f) }

    // Morph factor: 1f = horizontal cluster with detached dot, 0f = 6-dot circular ring
    val clusterFactor: Float = when {
        // First cycle starts immediately in/into the wave cluster!
        elapsedMs < 280L -> easeOutCubic(tMs / 280f)
        tMs < 350f -> easeInOutCubic(tMs / 350f)
        tMs < 1550f -> 1f
        tMs < 2150f -> 1f - easeInOutCubic((tMs - 1550f) / 600f)
        else -> 0f // Settled in 6-dot ring during 2150..7550ms (5s pause)
    }

    // Ring rotation during coil-in and settle (1550..2550ms)
    val ringRotationRad: Float = when {
        tMs < 1550f -> 0f
        tMs < 2550f -> easeOutCubic((tMs - 1550f) / 1000f) * (PI.toFloat() * (2f / 3f))
        else -> PI.toFloat() * (2f / 3f)
    }

    // Wave phase while in the horizontal cluster (0..1600ms)
    val waveProgress = (tMs / 1550f).coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .width(120.dp)
            .height(54.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringRadiusPx = 13.5.dp.toPx()
        val ringDotRadiusPx = 4.2.dp.toPx()

        // Traveling wave peak across the 5 left dots (indices 0..4)
        val peakIndex = 1.2f + sin(waveProgress * PI.toFloat() * 2f) * 1.4f

        for (i in 0 until 6) {
            // 1. Ring position (6-dot hexagon circle)
            val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / 6f) + ringRotationRad
            val ringX = cx + cos(angle) * ringRadiusPx
            val ringY = cy + sin(angle) * ringRadiusPx

            // 2. Horizontal wave + detached right dot position
            val waveLiftDp = if (i < 5) {
                val dist = i - peakIndex
                val swell = exp(-(dist * dist) / 1.1f)
                -swell * 3.5f
            } else {
                // Subtle float for the detached 6th dot on the right
                sin(waveProgress * PI.toFloat() * 2f) * 1.5f
            }

            val waveRadiusBoostDp = if (i < 5) {
                val dist = i - peakIndex
                exp(-(dist * dist) / 0.9f) * 1.6f
            } else {
                0f
            }

            val lineX = cx + clusterX[i].dp.toPx()
            val lineY = cy + (clusterBaseY[i] + waveLiftDp).dp.toPx()
            val lineR = (clusterBaseR[i] + waveRadiusBoostDp).dp.toPx()

            // Smooth interpolation between 6-dot ring and horizontal wave cluster
            val finalX = ringX + (lineX - ringX) * clusterFactor
            val finalY = ringY + (lineY - ringY) * clusterFactor
            val finalR = ringDotRadiusPx + (lineR - ringDotRadiusPx) * clusterFactor

            drawCircle(
                color = dotColor,
                radius = finalR,
                center = Offset(finalX, finalY)
            )
        }
    }
}

@Composable
fun ConnectingWaveScreen(
    isEnglish: Boolean
) {
    val colors = LocalAstralColors.current
    val elapsedMs = rememberMonotonicMillis()
    val phase = (elapsedMs / 170.0).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Smooth 7-dot sine wave from Screenrecording 00:06-00:07
            Canvas(
                modifier = Modifier
                    .width(176.dp)
                    .height(46.dp)
            ) {
                val dotCount = 7
                val spacing = size.width / (dotCount + 1)
                val centerY = size.height / 2f
                val amplitude = 7.dp.toPx()

                for (i in 0 until dotCount) {
                    val x = spacing * (i + 1)
                    val yOffset = sin(phase - i * 0.62f) * amplitude
                    drawCircle(
                        color = colors.primaryInk,
                        radius = 4.2.dp.toPx(),
                        center = Offset(x, centerY + yOffset)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "connecting",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 1.4.sp,
                color = colors.secondaryText
            )
        }

        Text(
            text = "ASTRAL",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 54.sp,
            letterSpacing = 2.sp,
            color = colors.primaryInk,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Exact "No internet connection" overlay from main_9695194f.mp4:
 * - Plays the 6-dot animation immediately upon appearing
 * - Waits 5 seconds after completion before replaying
 * - Clicking "Try again" replays the animation immediately and re-checks connectivity
 */
@Composable
fun NoInternetConnectionOverlay(
    isEnglish: Boolean,
    onRetry: () -> Unit
) {
    val colors = LocalAstralColors.current
    var replayTrigger by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.90f))
            .clickable(enabled = true, onClick = {})
            .padding(28.dp)
            .testTag("no_internet_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            NoInternetSixDotsAnimation(
                dotColor = colors.primaryInk,
                triggerKey = replayTrigger,
                pauseBetweenCyclesMs = 5000L
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEnglish) "No internet connection" else "No internet connection",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.primaryInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEnglish) {
                    "Reconnect to a stable network to continue"
                } else {
                    "Reconnect to a stable network to continue"
                },
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
                    text = if (isEnglish) "Try again" else "Try again",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
