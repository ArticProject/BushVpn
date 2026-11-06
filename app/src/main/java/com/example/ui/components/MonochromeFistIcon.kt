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
