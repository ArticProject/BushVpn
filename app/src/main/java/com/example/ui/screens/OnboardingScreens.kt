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
import com.example.ui.components.MonochromeFistIcon
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
        // Top Header: BUSH VPN + Monochrome B&W Fist + Theme & RU/EN Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BUSH VPN",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp,
                    color = colors.primaryInk
                )
                Spacer(modifier = Modifier.width(8.dp))
                MonochromeFistIcon(
                    color = colors.primaryInk,
                    size = 22.dp
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
 * 60fps Motion-Captured 2D Rigid-Body Gravity & Collision Trajectory from main_9695194f.mp4.
 * Each entry has 12 floats: (x0, y0, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5) normalized to R_ring = 1.0f.
 * - Frame 0: Exact 6-dot hexagon circle.
 * - Frames 1..49 (816ms at 60fps): Gravity pulls the 6 balls down onto the floor (y = +1.407).
 *   Bottom balls hit the floor and bounce; top-right ball deflects off bottom-right ball and rolls
 *   out to the right (x = +2.415); top-center and top-left balls collide and settle into the upper
 *   pockets between the floor balls while pushing the bottom-left ball outward (x = -1.267).
 */
private val SIX_BALL_PHYSICS_FRAMES: Array<FloatArray> = arrayOf(
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.499f, 0.865f, 0.499f, 0.000f, 0.998f, -0.865f, 0.498f), // 00
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.498f, 0.866f, 0.498f, 0.000f, 0.998f, -0.865f, 0.498f), // 01
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.499f, 0.866f, 0.500f, 0.000f, 0.998f, -0.865f, 0.498f), // 02
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.498f, 0.865f, 0.509f, 0.000f, 0.998f, -0.865f, 0.498f), // 03
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.494f, 0.865f, 0.525f, 0.000f, 0.998f, -0.865f, 0.498f), // 04
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.487f, 0.865f, 0.546f, 0.000f, 0.999f, -0.865f, 0.498f), // 05
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.478f, 0.866f, 0.576f, 0.000f, 1.002f, -0.865f, 0.498f), // 06
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.466f, 0.865f, 0.611f, 0.000f, 1.005f, -0.865f, 0.498f), // 07
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.451f, 0.865f, 0.659f, 0.000f, 1.014f, -0.866f, 0.498f), // 08
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.865f, -0.434f, 0.866f, 0.715f, 0.001f, 1.027f, -0.866f, 0.499f), // 09
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.866f, -0.414f, 0.865f, 0.785f, 0.000f, 1.045f, -0.866f, 0.500f), // 10
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.002f, 0.865f, -0.389f, 0.865f, 0.872f, 0.000f, 1.071f, -0.866f, 0.501f), // 11
    floatArrayOf(-0.866f, -0.501f, 0.000f, -1.001f, 0.865f, -0.361f, 0.865f, 0.971f, 0.000f, 1.100f, -0.865f, 0.503f), // 12
    floatArrayOf(-0.866f, -0.498f, 0.000f, -0.999f, 0.865f, -0.328f, 0.866f, 1.096f, 0.000f, 1.136f, -0.867f, 0.509f), // 13
    floatArrayOf(-0.867f, -0.494f, 0.000f, -0.995f, 0.866f, -0.289f, 0.866f, 1.257f, -0.001f, 1.182f, -0.865f, 0.517f), // 14
    floatArrayOf(-0.866f, -0.489f, 0.000f, -0.992f, 0.866f, -0.242f, 0.866f, 1.379f, -0.001f, 1.238f, -0.866f, 0.529f), // 15
    floatArrayOf(-0.866f, -0.482f, 0.000f, -0.983f, 0.866f, -0.187f, 0.866f, 1.326f, 0.000f, 1.317f, -0.866f, 0.545f), // 16
    floatArrayOf(-0.866f, -0.472f, 0.000f, -0.969f, 0.866f, -0.120f, 0.866f, 1.295f, 0.000f, 1.397f, -0.866f, 0.569f), // 17
    floatArrayOf(-0.866f, -0.462f, 0.000f, -0.946f, 0.866f, -0.040f, 0.865f, 1.278f, 0.000f, 1.369f, -0.866f, 0.604f), // 18
    floatArrayOf(-0.866f, -0.448f, 0.001f, -0.912f, 0.866f, 0.066f, 0.866f, 1.272f, -0.001f, 1.354f, -0.866f, 0.649f), // 19
    floatArrayOf(-0.866f, -0.434f, 0.000f, -0.861f, 0.866f, 0.206f, 0.865f, 1.277f, -0.001f, 1.349f, -0.865f, 0.708f), // 20
    floatArrayOf(-0.866f, -0.414f, 0.001f, -0.786f, 0.866f, 0.505f, 0.866f, 1.293f, 0.000f, 1.347f, -0.865f, 0.791f), // 21
    floatArrayOf(-0.866f, -0.393f, 0.000f, -0.690f, 0.905f, 0.494f, 0.865f, 1.319f, 0.000f, 1.353f, -0.865f, 0.895f), // 22
    floatArrayOf(-0.866f, -0.368f, 0.001f, -0.552f, 1.000f, 0.467f, 0.866f, 1.361f, 0.000f, 1.364f, -0.865f, 1.033f), // 23
    floatArrayOf(-0.866f, -0.340f, 0.001f, -0.361f, 1.134f, 0.454f, 0.866f, 1.396f, 0.000f, 1.384f, -0.865f, 1.216f), // 24
    floatArrayOf(-0.866f, -0.308f, 0.000f, -0.108f, 1.287f, 0.478f, 0.865f, 1.384f, 0.000f, 1.401f, -0.871f, 1.404f), // 25
    floatArrayOf(-0.865f, -0.271f, 0.002f, 0.233f, 1.445f, 0.565f, 0.866f, 1.382f, 0.000f, 1.395f, -0.940f, 1.375f), // 26
    floatArrayOf(-0.866f, -0.229f, 0.008f, 0.621f, 1.590f, 0.739f, 0.866f, 1.387f, 0.000f, 1.396f, -0.993f, 1.367f), // 27
    floatArrayOf(-0.866f, -0.180f, 0.114f, 0.609f, 1.708f, 1.031f, 0.866f, 1.404f, 0.000f, 1.402f, -1.030f, 1.374f), // 28
    floatArrayOf(-0.866f, -0.124f, 0.214f, 0.603f, 1.786f, 1.389f, 0.866f, 1.401f, 0.000f, 1.405f, -1.053f, 1.388f), // 29
    floatArrayOf(-0.865f, -0.060f, 0.304f, 0.614f, 1.884f, 1.361f, 0.866f, 1.401f, 0.000f, 1.405f, -1.063f, 1.401f), // 30
    floatArrayOf(-0.865f, 0.015f, 0.381f, 0.636f, 1.961f, 1.351f, 0.866f, 1.404f, 0.000f, 1.405f, -1.079f, 1.406f), // 31
    floatArrayOf(-0.866f, 0.117f, 0.453f, 0.665f, 2.032f, 1.351f, 0.867f, 1.405f, 0.000f, 1.405f, -1.128f, 1.406f), // 32
    floatArrayOf(-0.865f, 0.225f, 0.497f, 0.687f, 2.086f, 1.355f, 0.866f, 1.405f, 0.000f, 1.406f, -1.152f, 1.406f), // 33
    floatArrayOf(-0.865f, 0.388f, 0.527f, 0.703f, 2.133f, 1.357f, 0.866f, 1.407f, 0.000f, 1.406f, -1.171f, 1.406f), // 34
    floatArrayOf(-0.865f, 0.708f, 0.534f, 0.709f, 2.176f, 1.362f, 0.867f, 1.407f, 0.000f, 1.406f, -1.185f, 1.407f), // 35
    floatArrayOf(-0.838f, 0.714f, 0.485f, 0.714f, 2.212f, 1.366f, 0.866f, 1.406f, 0.000f, 1.406f, -1.199f, 1.407f), // 36
    floatArrayOf(-0.773f, 0.713f, 0.436f, 0.715f, 2.245f, 1.372f, 0.866f, 1.406f, 0.000f, 1.405f, -1.209f, 1.406f), // 37
    floatArrayOf(-0.694f, 0.727f, 0.388f, 0.719f, 2.273f, 1.375f, 0.866f, 1.406f, 0.000f, 1.406f, -1.218f, 1.406f), // 38
    floatArrayOf(-0.619f, 0.761f, 0.347f, 0.723f, 2.297f, 1.379f, 0.866f, 1.406f, -0.001f, 1.408f, -1.226f, 1.406f), // 39
    floatArrayOf(-0.573f, 0.823f, 0.379f, 0.739f, 2.319f, 1.384f, 0.867f, 1.407f, 0.000f, 1.407f, -1.234f, 1.406f), // 40
    floatArrayOf(-0.576f, 0.900f, 0.402f, 0.752f, 2.337f, 1.388f, 0.866f, 1.406f, 0.001f, 1.408f, -1.240f, 1.406f), // 41
    floatArrayOf(-0.580f, 0.903f, 0.420f, 0.761f, 2.352f, 1.389f, 0.866f, 1.406f, 0.001f, 1.407f, -1.245f, 1.406f), // 42
    floatArrayOf(-0.588f, 0.910f, 0.432f, 0.768f, 2.365f, 1.391f, 0.867f, 1.407f, 0.000f, 1.407f, -1.250f, 1.406f), // 43
    floatArrayOf(-0.598f, 0.921f, 0.432f, 0.767f, 2.377f, 1.395f, 0.867f, 1.407f, 0.000f, 1.407f, -1.253f, 1.406f), // 44
    floatArrayOf(-0.609f, 0.934f, 0.432f, 0.768f, 2.388f, 1.395f, 0.867f, 1.407f, 0.000f, 1.407f, -1.257f, 1.406f), // 45
    floatArrayOf(-0.618f, 0.947f, 0.432f, 0.768f, 2.397f, 1.396f, 0.867f, 1.407f, 0.000f, 1.407f, -1.259f, 1.406f), // 46
    floatArrayOf(-0.628f, 0.959f, 0.432f, 0.768f, 2.404f, 1.398f, 0.867f, 1.407f, 0.001f, 1.407f, -1.261f, 1.406f), // 47
    floatArrayOf(-0.633f, 0.966f, 0.432f, 0.768f, 2.411f, 1.400f, 0.867f, 1.407f, 0.000f, 1.407f, -1.266f, 1.406f), // 48
    floatArrayOf(-0.634f, 0.970f, 0.432f, 0.768f, 2.415f, 1.400f, 0.867f, 1.407f, 0.000f, 1.407f, -1.267f, 1.406f)  // 49
)

/**
 * Exact 6-Ball Gravity Collapse & Collision Animation from main_9695194f.mp4:
 * - Plays IMMEDIATELY when the window appears (and every time "Try again" is pressed):
 *   1. 0..780ms (Rise): The 6 balls rise up from the scattered floor pile into the 6-dot circle (frames 49 -> 0).
 *   2. 780..1380ms (Spin): The 6-dot circle rotates smoothly by 120°/180° trying to establish connection.
 *   3. 1380..2260ms (Gravity Collapse & Scatter): Gravity pulls all 6 balls down onto the floor (frames 0 -> 49);
 *      bottom balls hit the floor and bounce, right ball slides off and rolls to the right, and top balls
 *      collide into the upper pockets while pushing the left ball outward.
 *   4. 2260..7260ms (5000ms = 5s wait): The 6 balls lie scattered on the floor for 5 seconds before repeating.
 */
@Composable
fun NoInternetSixDotsAnimation(
    dotColor: Color,
    triggerKey: Int = 0,
    pauseBetweenCyclesMs: Long = 5000L,
    modifier: Modifier = Modifier
) {
    val elapsedMs = rememberMonotonicMillis(resetKey = triggerKey)
    val riseDurationMs = 780L
    val spinDurationMs = 600L
    val fallDurationMs = 880L
    val activeTotalMs = riseDurationMs + spinDurationMs + fallDurationMs
    val totalCycleMs = activeTotalMs + pauseBetweenCyclesMs
    val tMs = elapsedMs % totalCycleMs

    val maxIndex = (SIX_BALL_PHYSICS_FRAMES.size - 1).toFloat()

    // Continuous frame index in [0f .. 49f], where 0f = 6-dot circle and 49f = scattered floor pile
    val exactFramePos: Float = when {
        tMs < riseDurationMs -> {
            val p = easeInOutCubic(tMs.toFloat() / riseDurationMs.toFloat())
            (1f - p) * maxIndex
        }
        tMs < riseDurationMs + spinDurationMs -> 0f
        tMs < activeTotalMs -> {
            val fallElapsed = (tMs - (riseDurationMs + spinDurationMs)).toFloat()
            val p = (fallElapsed / fallDurationMs.toFloat()).coerceIn(0f, 1f)
            p * maxIndex
        }
        else -> maxIndex
    }

    // Smooth spinner rotation during the Rise + Spin phase (rotates an exact multiple of 60° = PI/3
    // so that at the end of Spin the 6 balls align seamlessly with Frame 0 of the gravity fall!)
    val spinAngleRad: Float = when {
        tMs < riseDurationMs -> {
            val p = easeInOutCubic(tMs.toFloat() / riseDurationMs.toFloat())
            // Blend rotation in as the balls approach the circle
            p * p * (PI.toFloat() / 3f)
        }
        tMs < riseDurationMs + spinDurationMs -> {
            val p = easeInOutCubic((tMs - riseDurationMs).toFloat() / spinDurationMs.toFloat())
            (PI.toFloat() / 3f) + p * (2f * PI.toFloat() / 3f)
        }
        else -> 0f
    }

    // Weight of how close we are to the pure circle (1f in circle, 0f on floor) for applying spinAngleRad
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
        // Exact ball-to-ring radius ratio from main_9695194f.mp4 (20.94 / 53.45 = 0.3918)
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BUSH",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 52.sp,
                letterSpacing = 2.sp,
                color = colors.primaryInk
            )
            Spacer(modifier = Modifier.width(12.dp))
            MonochromeFistIcon(
                color = colors.primaryInk,
                size = 44.dp
            )
        }
    }
}

@Composable
fun NoInternetConnectionOverlay(
    isEnglish: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val colors = LocalAstralColors.current
    var replayTrigger by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.96f))
            .clickable(enabled = true, onClick = onDismiss)
            .padding(28.dp)
            .testTag("no_internet_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = true, onClick = {})
                .padding(horizontal = 16.dp)
        ) {
            NoInternetSixDotsAnimation(
                dotColor = colors.primaryInk,
                triggerKey = replayTrigger,
                pauseBetweenCyclesMs = 5000L
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
