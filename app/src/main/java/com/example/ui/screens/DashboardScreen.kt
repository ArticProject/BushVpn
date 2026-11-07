package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnTunnelSettings
import com.example.ui.CoreLogEntry
import com.example.ui.VpnConnectionStatus
import com.example.ui.components.IncySettingsGearIcon
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    connectionStatus: VpnConnectionStatus,
    connectedSeconds: Long,
    selectedServer: VlessServerEntity?,
    allServers: List<VlessServerEntity>,
    isEnglish: Boolean,
    isDarkTheme: Boolean,
    isPingingAll: Boolean = false,
    pingingServerIds: Set<Long> = emptySet(),
    settings: VpnTunnelSettings = VpnTunnelSettings(),
    coreLogs: List<CoreLogEntry> = emptyList(),
    onUpdateSettings: ((VpnTunnelSettings) -> VpnTunnelSettings) -> Unit = {},
    onClearLogs: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleConnection: () -> Unit,
    onSelectServer: (VlessServerEntity) -> Unit,
    onOpenServerList: () -> Unit,
    onOpenAddServer: () -> Unit,
    onOpenXrayConfig: (VlessServerEntity) -> Unit,
    onPingServer: (VlessServerEntity) -> Unit = {},
    onPingAll: () -> Unit = {},
    onImportSubscriptionText: (String) -> Unit = {},
    onCopiedMessage: (String) -> Unit = {},
    onDeleteServer: (VlessServerEntity) -> Unit = {},
    onReplayOnboarding: () -> Unit = {},
    onTriggerNoInternetDemo: () -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    val activeNode = selectedServer ?: allServers.firstOrNull()

    var allExpanded by remember(settings.collapseSubsByDefault) {
        mutableStateOf(!settings.collapseSubsByDefault)
    }
    var showHappSettings by remember { mutableStateOf(false) }

    val groupedProviders = remember(allServers) {
        allServers.groupBy { srv ->
            if (!srv.isCustom) "BUSH" else srv.providerName.ifBlank { "CUSTOM VLESS" }
        }
    }
    val providerCount = groupedProviders.size.coerceAtLeast(1)

    val infiniteTransition = rememberInfiniteTransition(label = "tactile_orb_transition")
    val sweepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepPhase"
    )
    val pulseRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadiusFactor"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header Bar: Monochrome Fist + BUSH VPN on left | Settings + Offline Demo + Theme on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonochromeFistIcon(
                        color = colors.primaryInk,
                        size = 24.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "BUSH VPN",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 0.6.sp,
                        color = colors.primaryInk
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showHappSettings = true }
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("dashboard_settings_button"),
                        color = colors.card,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IncySettingsGearIcon(
                                color = colors.primaryInk,
                                size = 18.dp
                            )
                        }
                    }

                    IconButton(
                        onClick = onTriggerNoInternetDemo,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.card)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("header_offline_demo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "No internet animation",
                            tint = colors.secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.card)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("header_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = colors.primaryInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sculptural Tactile Multi-Ring Connect Button (Happ / Incy Pro style)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(216.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 1: Precision 48-Tick Radial Dial & Subtle Pulse Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val tickCount = 48
                        val innerTickR = 90.dp.toPx()
                        val outerTickR = 98.dp.toPx()

                        if (connectionStatus == VpnConnectionStatus.CONNECTED) {
                            drawCircle(
                                color = colors.primaryInk.copy(alpha = 0.08f),
                                radius = 84.dp.toPx() * pulseRadiusFactor,
                                center = Offset(cx, cy)
                            )
                        }

                        for (i in 0 until tickCount) {
                            val fraction = i.toFloat() / tickCount.toFloat()
                            val angleRad = (fraction * 2f * PI.toFloat()) - (PI.toFloat() / 2f)

                            val tickAlpha = when (connectionStatus) {
                                VpnConnectionStatus.DISCONNECTED -> {
                                    val wave = ((sin((fraction - sweepPhase) * 2f * PI.toFloat()) + 1f) * 0.5f)
                                    0.12f + 0.14f * wave
                                }
                                VpnConnectionStatus.CONNECTING -> {
                                    val dist = ((fraction - sweepPhase + 1f) % 1f)
                                    if (dist < 0.35f) 0.18f + (1f - dist / 0.35f) * 0.78f else 0.14f
                                }
                                VpnConnectionStatus.CONNECTED -> {
                                    val wave = ((sin((fraction - sweepPhase) * 2f * PI.toFloat()) + 1f) * 0.5f)
                                    0.35f + 0.55f * wave
                                }
                            }

                            val tickColor = if (connectionStatus == VpnConnectionStatus.CONNECTED && i % 4 == 0) {
                                colors.activeGreen.copy(alpha = tickAlpha)
                            } else {
                                colors.primaryInk.copy(alpha = tickAlpha)
                            }

                            val startX = cx + cos(angleRad) * innerTickR
                            val startY = cy + sin(angleRad) * innerTickR
                            val endX = cx + cos(angleRad) * outerTickR
                            val endY = cy + sin(angleRad) * outerTickR

                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Layer 2: Tactile Outer Bezel Ring (168.dp)
                    Surface(
                        modifier = Modifier
                            .size(168.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.16f)
                            )
                            .border(
                                width = 1.2.dp,
                                color = if (connectionStatus == VpnConnectionStatus.CONNECTED) {
                                    colors.primaryInk.copy(alpha = 0.45f)
                                } else {
                                    colors.border
                                },
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        color = colors.card
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Layer 3: Inner Core Power Button (134.dp)
                            val isActive = connectionStatus != VpnConnectionStatus.DISCONNECTED
                            Surface(
                                modifier = Modifier
                                    .size(134.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onToggleConnection)
                                    .border(
                                        width = if (isActive) 2.dp else 1.8.dp,
                                        color = colors.primaryInk,
                                        shape = CircleShape
                                    )
                                    .testTag("vpn_connect_orb"),
                                shape = CircleShape,
                                color = if (isActive) colors.primaryInk else colors.surface
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val emblemColor = if (isActive) colors.onPrimaryInk else colors.primaryInk

                                    // Crisp Custom Drawn Power Emblem
                                    Canvas(modifier = Modifier.size(38.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val strokeW = 2.8.dp.toPx()
                                        val pad = w * 0.16f

                                        // Power arc (300 degrees, open at top)
                                        drawArc(
                                            color = emblemColor,
                                            startAngle = -60f,
                                            sweepAngle = 300f,
                                            useCenter = false,
                                            topLeft = Offset(pad, pad + h * 0.04f),
                                            size = Size(w - pad * 2f, h - pad * 2f),
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                        )

                                        // Vertical top power stem
                                        drawLine(
                                            color = emblemColor,
                                            start = Offset(w / 2f, h * 0.10f),
                                            end = Offset(w / 2f, h * 0.48f),
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = when (connectionStatus) {
                                            VpnConnectionStatus.DISCONNECTED -> if (isEnglish) "CONNECT" else "ПОДКЛЮЧИТЬ"
                                            VpnConnectionStatus.CONNECTING -> if (isEnglish) "LINKING..." else "СОЕДИНЕНИЕ"
                                            VpnConnectionStatus.CONNECTED -> formatCompactTimer(connectedSeconds)
                                        },
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = 1.2.sp,
                                        color = emblemColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Country & Status Pill right below the Connect Orb
                if (activeNode != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onPingServer(activeNode) }
                            .border(1.dp, colors.border, RoundedCornerShape(50)),
                        color = colors.card,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (connectionStatus == VpnConnectionStatus.CONNECTED) {
                                            colors.activeGreen
                                        } else {
                                            colors.primaryInk.copy(alpha = 0.45f)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${activeNode.flagEmoji} ${activeNode.displayTitle(isEnglish)}",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.primaryInk
                            )
                            Text(
                                text = "  •  ${activeNode.pingMs} ms",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.secondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Incy-Style "Servers" Header + Moved Action Buttons (^, Ping, +) right next to each other
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Servers",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = colors.primaryInk
                    )

                    IncyTopActionButton(
                        icon = if (allExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse / Expand",
                        onClick = { allExpanded = !allExpanded },
                        testTag = "dash_incy_collapse_button"
                    )

                    IncyTopPingActionButton(
                        onClick = onPingAll,
                        testTag = "dash_incy_ping_button"
                    )

                    Surface(
                        modifier = Modifier
                            .width(40.dp)
                            .height(35.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable(onClick = onOpenAddServer)
                            .testTag("dash_incy_add_button"),
                        color = colors.primaryInk,
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Subscription or Key",
                                tint = colors.onPrimaryInk,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${allServers.size} servers · $providerCount providers",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colors.secondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Incy Provider Cards on Tab 1
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedProviders.entries.forEach { (providerName, groupNodes) ->
                    val isDefaultBush = providerName == "BUSH"
                    IncyProviderGroupCard(
                        providerTitle = providerName,
                        lastUpdatedLabel = if (isDefaultBush) "1h" else "now",
                        leftStatusLabel = "∞",
                        trafficText = if (isDefaultBush) "1.42 GB / ∞" else "0.18 GB / ∞",
                        trafficProgress = if (isDefaultBush) 0.24f else 0.12f,
                        infoLines = if (isDefaultBush) {
                            listOf(if (isEnglish) "Soon" else "Скоро")
                        } else {
                            listOf(if (isEnglish) "Active subscription" else "Активная подписка")
                        },
                        supportLabel = if (isDefaultBush) "Support" else null,
                        timestampText = "06.11.26, 13:15",
                        nodes = groupNodes,
                        selectedServerId = activeNode?.id,
                        pingingServerIds = pingingServerIds,
                        isEnglish = isEnglish,
                        forceExpanded = allExpanded,
                        onRefreshGroup = onPingAll,
                        onPingGroup = onPingAll,
                        onAddKeyToGroup = onOpenAddServer,
                        onPasteFromClipboard = {
                            val clip = clipboardManager.getText()?.text?.trim().orEmpty()
                            if (clip.isNotEmpty()) {
                                onImportSubscriptionText(clip)
                            } else {
                                onOpenAddServer()
                            }
                        },
                        onSelectNode = onSelectServer,
                        onPingNode = onPingServer,
                        onInspectNode = onOpenXrayConfig,
                        onDeleteNode = onDeleteServer,
                        onCopyNodeUri = { srv ->
                            clipboardManager.setText(AnnotatedString(VlessParser.toUri(srv)))
                            onCopiedMessage("VLESS URI скопирован")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 96.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BUSH",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 58.sp,
                    letterSpacing = 3.sp,
                    color = colors.watermark,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(12.dp))
                MonochromeFistIcon(
                    color = colors.watermark,
                    size = 48.dp
                )
            }
        }

        // Full-Screen 1:1 Happ Proxy Utility Settings Overlay
        AnimatedVisibility(
            visible = showHappSettings,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            HappSettingsScreen(
                settings = settings,
                coreLogs = coreLogs,
                isEnglish = isEnglish,
                isDarkTheme = isDarkTheme,
                onUpdateSettings = onUpdateSettings,
                onToggleLanguage = onToggleLanguage,
                onToggleTheme = onToggleTheme,
                onClearLogs = onClearLogs,
                onReplayOnboarding = {
                    showHappSettings = false
                    onReplayOnboarding()
                },
                onCopiedMessage = onCopiedMessage,
                onBack = { showHappSettings = false }
            )
        }
    }
}

private fun formatCompactTimer(totalSeconds: Long): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
