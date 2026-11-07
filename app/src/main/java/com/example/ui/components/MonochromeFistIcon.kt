package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure monochrome (Black & White) geometric oncoming fist icon 👊
 * Renders strictly in [color] with crisp negative-space gaps so it never uses colored system emojis.
 */
@Composable
fun MonochromeFistIcon(
    color: Color,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val gap = w * 0.045f

        // 1. Four vertical curled fingers (index, middle, ring, pinky)
        val fingerWidths = floatArrayOf(w * 0.175f, w * 0.185f, w * 0.175f, w * 0.155f)
        val fingerTops = floatArrayOf(h * 0.16f, h * 0.11f, h * 0.14f, h * 0.21f)
        val fingerBottoms = floatArrayOf(h * 0.50f, h * 0.50f, h * 0.52f, h * 0.54f)
        var curX = w * 0.16f

        for (i in 0 until 4) {
            val fw = fingerWidths[i]
            val top = fingerTops[i]
            val bot = fingerBottoms[i]
            drawRoundRect(
                color = color,
                topLeft = Offset(curX, top),
                size = Size(fw, bot - top),
                cornerRadius = CornerRadius(fw * 0.46f, fw * 0.46f)
            )
            curX += fw + gap
        }

        // 2. Tucked horizontal thumb locking across the front of the fingers
        val thumbPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.08f,
                    top = h * 0.46f,
                    right = w * 0.64f,
                    bottom = h * 0.64f,
                    cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
                )
            )
        }
        drawPath(path = thumbPath, color = color)

        // 3. Lower solid fist metacarpal / palm base silhouette
        val basePath = Path().apply {
            moveTo(w * 0.14f, h * 0.68f)
            lineTo(w * 0.68f, h * 0.68f)
            lineTo(w * 0.68f, h * 0.58f)
            lineTo(w * 0.88f, h * 0.58f)
            quadraticTo(w * 0.88f, h * 0.82f, w * 0.72f, h * 0.90f)
            lineTo(w * 0.28f, h * 0.90f)
            quadraticTo(w * 0.14f, h * 0.84f, w * 0.14f, h * 0.68f)
            close()
        }
        drawPath(path = basePath, color = color)

        // Subtle outer knuckle frame line for sharp monochrome definition
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.46f),
            size = Size(w * 0.56f, h * 0.18f),
            cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
            style = Stroke(width = w * 0.02f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Custom Incy-style Ping / Speedometer Gauge Icon used across the entire app
 * (top Servers action bars, Provider cards, and server rows).
 */
@Composable
fun IncyPingGaugeIcon(
    color: Color,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (w * 0.105f).coerceAtLeast(1.8f)

        val arcPadding = w * 0.11f
        val arcSize = Size(w - arcPadding * 2f, h - arcPadding * 2f)

        // Outer speedometer arc (155° to 385°, open at bottom-right just like Incy's gauge icon)
        drawArc(
            color = color,
            startAngle = 145f,
            sweepAngle = 215f,
            useCenter = false,
            topLeft = Offset(arcPadding, arcPadding + h * 0.05f),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom flat base line of the Incy speedometer icon
        val baseY = h * 0.76f
        drawLine(
            color = color,
            start = Offset(w * 0.20f, baseY),
            end = Offset(w * 0.80f, baseY),
            strokeWidth = strokeWidth * 0.95f,
            cap = StrokeCap.Round
        )

        // Needle pointing to ~42° (top-right) from center pivot
        val pivot = Offset(w * 0.50f, h * 0.62f)
        val needleTip = Offset(w * 0.76f, h * 0.30f)
        drawLine(
            color = color,
            start = pivot,
            end = needleTip,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Center pivot dot
        drawCircle(
            color = color,
            radius = strokeWidth * 0.82f,
            center = pivot
        )
    }
}

/**
 * Crisp custom Settings / Gear icon for the top-left of Bush VPN.
 */
@Composable
fun IncySettingsGearIcon(
    color: Color,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = (w * 0.105f).coerceAtLeast(1.8f)
        val outerR = w * 0.30f
        val innerR = w * 0.15f

        // 8 gear teeth
        for (i in 0 until 8) {
            val angle = (i * (PI / 4.0)).toFloat()
            val x1 = cx + cos(angle) * (outerR * 0.85f)
            val y1 = cy + sin(angle) * (outerR * 0.85f)
            val x2 = cx + cos(angle) * (w * 0.43f)
            val y2 = cy + sin(angle) * (w * 0.43f)
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = stroke * 1.05f,
                cap = StrokeCap.Round
            )
        }

        // Gear body circle
        drawCircle(
            color = color,
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = stroke)
        )

        // Inner hub
        drawCircle(
            color = color,
            radius = innerR * 0.55f,
            center = Offset(cx, cy)
        )
    }
}
