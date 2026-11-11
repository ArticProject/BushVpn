package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnTunnelSettings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

/**
 * Full-bleed Canvas-painted rounded square in the colors/stripes/crosses of the country's flag.
 * Replaces emoji flags and has no separate click listener so tapping it never opens a modal.
 */
@Composable
fun CountryFlagSquare(
    countryCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    cornerRadius: Dp = 12.dp,
    isSelected: Boolean = false
) {
    val colors = LocalAstralColors.current
    val iso = countryCode.uppercase().trim()

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) colors.primaryInk else colors.border.copy(alpha = 0.65f),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            fun horizontalThree(c1: Color, c2: Color, c3: Color) {
                drawRect(c1, Offset(0f, 0f), Size(w, h / 3f + 1f))
                drawRect(c2, Offset(0f, h / 3f), Size(w, h / 3f + 1f))
                drawRect(c3, Offset(0f, h * 2f / 3f), Size(w, h / 3f))
            }

            fun verticalThree(c1: Color, c2: Color, c3: Color) {
                drawRect(c1, Offset(0f, 0f), Size(w / 3f + 1f, h))
                drawRect(c2, Offset(w / 3f, 0f), Size(w / 3f + 1f, h))
                drawRect(c3, Offset(w * 2f / 3f, 0f), Size(w / 3f, h))
            }

            fun horizontalTwo(c1: Color, c2: Color) {
                drawRect(c1, Offset(0f, 0f), Size(w, h / 2f + 1f))
                drawRect(c2, Offset(0f, h / 2f), Size(w, h / 2f))
            }

            fun nordicCross(bg: Color, cross: Color, innerCross: Color? = null) {
                drawRect(bg, Offset.Zero, Size(w, h))
                val barW = w * 0.22f
                val vx = w * 0.30f
                val hy = (h - barW) / 2f
                drawRect(cross, Offset(vx, 0f), Size(barW, h))
                drawRect(cross, Offset(0f, hy), Size(w, barW))
                if (innerCross != null) {
                    val innerW = barW * 0.52f
                    val pad = (barW - innerW) / 2f
                    drawRect(innerCross, Offset(vx + pad, 0f), Size(innerW, h))
                    drawRect(innerCross, Offset(0f, hy + pad), Size(w, innerW))
                }
            }

            when (iso) {
                "FI" -> nordicCross(Color(0xFFF5F7FA), Color(0xFF003580))
                "SE" -> nordicCross(Color(0xFF006AA7), Color(0xFFFECC02))
                "NO" -> nordicCross(Color(0xFFBA0C2F), Color(0xFFF5F7FA), Color(0xFF00205B))
                "DK" -> nordicCross(Color(0xFFC8102E), Color(0xFFF5F7FA))
                "PL" -> horizontalTwo(Color(0xFFF5F7FA), Color(0xFFDC143C))
                "UA" -> horizontalTwo(Color(0xFF0057B7), Color(0xFFFFD700))
                "SG" -> horizontalTwo(Color(0xFFEF3340), Color(0xFFF5F7FA))
                "ID", "MC" -> horizontalTwo(Color(0xFFCE1126), Color(0xFFF5F7FA))
                "DE" -> horizontalThree(Color(0xFF18191C), Color(0xFFDD0000), Color(0xFFFFCE00))
                "NL" -> horizontalThree(Color(0xFFAE1C28), Color(0xFFF5F7FA), Color(0xFF21468B))
                "RU" -> horizontalThree(Color(0xFFF5F7FA), Color(0xFF0039A6), Color(0xFFD52B1E))
                "EE" -> horizontalThree(Color(0xFF0072CE), Color(0xFF18191C), Color(0xFFF5F7FA))
                "LT" -> horizontalThree(Color(0xFFFDB913), Color(0xFF006A44), Color(0xFFC1272D))
                "AT" -> horizontalThree(Color(0xFFED2939), Color(0xFFF5F7FA), Color(0xFFED2939))
                "HU" -> horizontalThree(Color(0xFFCE2939), Color(0xFFF5F7FA), Color(0xFF477050))
                "BG" -> horizontalThree(Color(0xFFF5F7FA), Color(0xFF00966E), Color(0xFFD62612))
                "RS" -> horizontalThree(Color(0xFFC6363C), Color(0xFF0C4076), Color(0xFFF5F7FA))
                "AM" -> horizontalThree(Color(0xFFD90012), Color(0xFF0033A0), Color(0xFFF2A800))
                "IN" -> horizontalThree(Color(0xFFFF9933), Color(0xFFF5F7FA), Color(0xFF138808))
                "FR" -> verticalThree(Color(0xFF002654), Color(0xFFF5F7FA), Color(0xFFED2939))
                "IT" -> verticalThree(Color(0xFF009246), Color(0xFFF5F7FA), Color(0xFFCE2B37))
                "BE" -> verticalThree(Color(0xFF18191C), Color(0xFFFDDA24), Color(0xFFEF3340))
                "IE" -> verticalThree(Color(0xFF169B62), Color(0xFFF5F7FA), Color(0xFFFF883E))
                "RO", "MD" -> verticalThree(Color(0xFF002B7F), Color(0xFFFCD116), Color(0xFFCE1126))
                "LV" -> {
                    drawRect(Color(0xFF9E3039), Offset.Zero, Size(w, h))
                    drawRect(Color(0xFFF5F7FA), Offset(0f, h * 0.40f), Size(w, h * 0.20f))
                }
                "ES" -> {
                    drawRect(Color(0xFFAA151B), Offset.Zero, Size(w, h))
                    drawRect(Color(0xFFF1BF00), Offset(0f, h * 0.25f), Size(w, h * 0.50f))
                }
                "CH" -> {
                    drawRect(Color(0xFFDA291C), Offset.Zero, Size(w, h))
                    val bar = w * 0.20f
                    val len = w * 0.58f
                    drawRect(Color(0xFFF5F7FA), Offset((w - bar) / 2f, (h - len) / 2f), Size(bar, len))
                    drawRect(Color(0xFFF5F7FA), Offset((w - len) / 2f, (h - bar) / 2f), Size(len, bar))
                }
                "GB", "UK" -> {
                    drawRect(Color(0xFF012169), Offset.Zero, Size(w, h))
                    // Diagonal white saltire
                    drawLine(Color(0xFFF5F7FA), Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.20f)
                    drawLine(Color(0xFFF5F7FA), Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.20f)
                    // Diagonal red saltire
                    drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.10f)
                    drawLine(Color(0xFFC8102E), Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.10f)
                    // White central cross
                    val wCross = w * 0.28f
                    drawRect(Color(0xFFF5F7FA), Offset((w - wCross) / 2f, 0f), Size(wCross, h))
                    drawRect(Color(0xFFF5F7FA), Offset(0f, (h - wCross) / 2f), Size(w, wCross))
                    // Red central cross
                    val rCross = w * 0.16f
                    drawRect(Color(0xFFC8102E), Offset((w - rCross) / 2f, 0f), Size(rCross, h))
                    drawRect(Color(0xFFC8102E), Offset(0f, (h - rCross) / 2f), Size(w, rCross))
                }
                "US" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    val stripeH = h / 7f
                    for (s in 0 until 7 step 2) {
                        drawRect(Color(0xFFB22234), Offset(0f, s * stripeH), Size(w, stripeH))
                    }
                    drawRect(Color(0xFF3C3B6E), Offset(0f, 0f), Size(w * 0.52f, h * 0.54f))
                }
                "CZ" -> {
                    horizontalTwo(Color(0xFFF5F7FA), Color(0xFFD7141A))
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w * 0.50f, h * 0.50f)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path, Color(0xFF11457E))
                }
                "TR" -> {
                    drawRect(Color(0xFFE30A17), Offset.Zero, Size(w, h))
                    drawCircle(Color(0xFFF5F7FA), radius = w * 0.24f, center = Offset(w * 0.45f, h * 0.50f))
                    drawCircle(Color(0xFFE30A17), radius = w * 0.19f, center = Offset(w * 0.51f, h * 0.50f))
                }
                "KZ" -> {
                    drawRect(Color(0xFF00AFCA), Offset.Zero, Size(w, h))
                    drawCircle(Color(0xFFFEC50C), radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.50f))
                }
                "JP" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    drawCircle(Color(0xFFBC002D), radius = w * 0.26f, center = Offset(w * 0.50f, h * 0.50f))
                }
                "KR" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    drawCircle(Color(0xFFCD2E3A), radius = w * 0.25f, center = Offset(w * 0.50f, h * 0.50f))
                    drawArc(
                        color = Color(0xFF0047A0),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.25f, h * 0.25f),
                        size = Size(w * 0.50f, h * 0.50f)
                    )
                }
                "AE" -> {
                    horizontalThree(Color(0xFF00732F), Color(0xFFF5F7FA), Color(0xFF18191C))
                    drawRect(Color(0xFFFF0000), Offset.Zero, Size(w * 0.28f, h))
                }
                "CA" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    drawRect(Color(0xFFFF0000), Offset.Zero, Size(w * 0.26f, h))
                    drawRect(Color(0xFFFF0000), Offset(w * 0.74f, 0f), Size(w * 0.26f, h))
                    drawCircle(Color(0xFFFF0000), radius = w * 0.15f, center = Offset(w * 0.50f, h * 0.50f))
                }
                "PT" -> {
                    drawRect(Color(0xFF006600), Offset.Zero, Size(w * 0.42f, h))
                    drawRect(Color(0xFFFF0000), Offset(w * 0.42f, 0f), Size(w * 0.58f, h))
                    drawCircle(Color(0xFFFFCC00), radius = w * 0.16f, center = Offset(w * 0.42f, h * 0.50f))
                }
                "GE" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    val bar = w * 0.20f
                    drawRect(Color(0xFFFF0000), Offset((w - bar) / 2f, 0f), Size(bar, h))
                    drawRect(Color(0xFFFF0000), Offset(0f, (h - bar) / 2f), Size(w, bar))
                }
                "IL" -> {
                    drawRect(Color(0xFFF5F7FA), Offset.Zero, Size(w, h))
                    drawRect(Color(0xFF0038B8), Offset(0f, h * 0.14f), Size(w, h * 0.14f))
                    drawRect(Color(0xFF0038B8), Offset(0f, h * 0.72f), Size(w, h * 0.14f))
                    drawCircle(Color(0xFF0038B8), radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.50f))
                }
                "HK" -> {
                    drawRect(Color(0xFFDE2910), Offset.Zero, Size(w, h))
                    drawCircle(Color(0xFFF5F7FA), radius = w * 0.20f, center = Offset(w * 0.50f, h * 0.50f))
                }
                "BR" -> {
                    drawRect(Color(0xFF009739), Offset.Zero, Size(w, h))
                    val path = Path().apply {
                        moveTo(w * 0.50f, h * 0.16f)
                        lineTo(w * 0.86f, h * 0.50f)
                        lineTo(w * 0.50f, h * 0.84f)
                        lineTo(w * 0.14f, h * 0.50f)
                        close()
                    }
                    drawPath(path, Color(0xFFFEDD00))
                    drawCircle(Color(0xFF012169), radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.50f))
                }
                else -> {
                    // Clean monochrome signal-bar badge for mobile-carrier / multi-region nodes
                    drawRect(Color(0xFF232631), Offset.Zero, Size(w, h))
                    val barW = w * 0.11f
                    val gap = w * 0.07f
                    val startX = (w - (4 * barW + 3 * gap)) / 2f
                    val heights = listOf(0.24f, 0.38f, 0.52f, 0.66f)
                    heights.forEachIndexed { index, ratio ->
                        val bh = h * ratio
                        val bx = startX + index * (barW + gap)
                        val by = h * 0.76f - bh
                        drawLine(
                            color = Color(0xFFF1F2F6),
                            start = Offset(bx + barW / 2f, h * 0.76f),
                            end = Offset(bx + barW / 2f, by),
                            strokeWidth = barW,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean iOS / T-Bank style subscription & key import sheet (replaces the cluttered dialog from Screenshot 3):
 * 1. One-tap primary button: "Вставить из буфера обмена" (immediately imports from clipboard)
 * 2. Minimalist single-line input field + "Добавить" button
 */
@Composable
fun AddOrEditVlessDialog(
    initialServer: VlessServerEntity? = null,
    onDismiss: () -> Unit,
    onImportUri: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onSaveCustomServer: (VlessServerEntity, Boolean) -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    var uriInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(1.dp, colors.border, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = colors.card
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // iOS drag handle pill
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(colors.border)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Новая подписка",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.primaryInk
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.cardElevated)
                                .clickable(onClick = onDismiss)
                                .testTag("close_add_server_dialog"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colors.secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Primary One-Tap Clipboard Import Button (iOS / T-Bank style)
                    Button(
                        onClick = {
                            val clip = clipboardManager.getText()?.text?.trim().orEmpty()
                            if (clip.isNotBlank()) {
                                onImportUri(clip)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("paste_clipboard_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryInk,
                            contentColor = colors.onPrimaryInk
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Вставить из буфера в 1 клик",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Compact Manual Link Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uriInput,
                            onValueChange = { uriInput = it },
                            placeholder = {
                                Text(
                                    text = "https://... или vless://...",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 13.sp,
                                    color = colors.mutedText
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vless_uri_input"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                color = colors.primaryInk
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryInk,
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Surface(
                            modifier = Modifier
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = uriInput.isNotBlank()) {
                                    onImportUri(uriInput.trim())
                                }
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .testTag("confirm_import_vless_button"),
                            color = if (uriInput.isNotBlank()) colors.primaryInk else colors.cardElevated,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = if (uriInput.isNotBlank()) colors.onPrimaryInk else colors.secondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XrayConfigInspectorDialog(
    server: VlessServerEntity,
    settings: VpnTunnelSettings,
    onDismiss: () -> Unit,
    onCopied: (String) -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    val vlessUri = remember(server) { VlessParser.toUri(server) }
    val xrayJson = remember(server, settings) { VlessParser.toXrayJsonConfig(server, settings) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .border(1.dp, colors.border, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${server.nodeAirportCode} • ${server.displayTitle(false)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.primaryInk
                        )
                        Text(
                            text = "${server.protocolBadge} • ${server.pingMs}ms",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = colors.secondaryText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(vlessUri))
                            onCopied("VLESS URI скопирован")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryInk,
                            contentColor = colors.onPrimaryInk
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy vless://", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(xrayJson))
                            onCopied("X-Ray JSON скопирован")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.primaryInk
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy JSON", fontSize = 12.sp, color = colors.primaryInk)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = xrayJson,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}
