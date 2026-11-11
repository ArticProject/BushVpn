package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WifiOff
import com.example.ui.components.iosBounceClickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnTunnelSettings
import com.example.ui.CoreLogEntry
import com.example.ui.VpnConnectionStatus
import com.example.ui.components.CountryFlagSquare
import com.example.ui.components.IncySettingsGearIcon
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale

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
    onToggleFavorite: (VlessServerEntity) -> Unit = {},
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

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_orb_transition")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathProgress"
    )

    val isActive = connectionStatus != VpnConnectionStatus.DISCONNECTED
    val buttonScale by animateFloatAsState(
        targetValue = when (connectionStatus) {
            VpnConnectionStatus.DISCONNECTED -> 1.0f
            VpnConnectionStatus.CONNECTING -> 0.96f
            VpnConnectionStatus.CONNECTED -> 1.03f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    val coreBgColor by animateColorAsState(
        targetValue = if (connectionStatus == VpnConnectionStatus.CONNECTED) {
            colors.primaryInk
        } else {
            colors.cardElevated
        },
        animationSpec = tween(280),
        label = "coreBgColor"
    )
    val coreContentColor by animateColorAsState(
        targetValue = if (connectionStatus == VpnConnectionStatus.CONNECTED) {
            colors.onPrimaryInk
        } else {
            colors.primaryInk
        },
        animationSpec = tween(280),
        label = "coreContentColor"
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

            // Top Header Bar: Left has Fist + BUSH VPN 2.0 AND Settings Gear underneath on the left; Right has Offline + Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NoteVaultFlowerIcon(
                            size = 25.dp,
                            color = colors.primaryInk,
                            rotationDegrees = orbitAngle * 0.2f,
                            strokeWidthDp = 2.0.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BushVPN",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp,
                            letterSpacing = (-0.3).sp,
                            color = colors.primaryInk
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Settings Gear Button on the left directly under the BUSH VPN title
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
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

            Spacer(modifier = Modifier.height(6.dp))

            // Pure Monochrome iOS / T-Bank Connect Button (White/Black inversion on connection)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 1: Monochrome Radial Glow & Orbital Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = (size.minDimension / 2f) * breathProgress

                        val glowAlpha = when (connectionStatus) {
                            VpnConnectionStatus.CONNECTED -> 0.14f
                            VpnConnectionStatus.CONNECTING -> 0.10f
                            VpnConnectionStatus.DISCONNECTED -> 0.04f
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.primaryInk.copy(alpha = glowAlpha), Color.Transparent),
                                center = centerOffset,
                                radius = maxRadius
                            ),
                            radius = maxRadius,
                            center = centerOffset
                        )

                        // Soft outer ring halo
                        drawCircle(
                            color = colors.border.copy(alpha = 0.65f),
                            radius = 90.dp.toPx() * (0.98f + (breathProgress - 1f) * 0.30f),
                            center = centerOffset,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        if (connectionStatus == VpnConnectionStatus.CONNECTING) {
                            drawArc(
                                color = colors.primaryInk.copy(alpha = 0.82f),
                                startAngle = orbitAngle,
                                sweepAngle = 115f,
                                useCenter = false,
                                topLeft = Offset(
                                    centerOffset.x - 84.dp.toPx(),
                                    centerOffset.y - 84.dp.toPx()
                                ),
                                size = Size(168.dp.toPx(), 168.dp.toPx()),
                                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else if (connectionStatus == VpnConnectionStatus.CONNECTED) {
                            drawCircle(
                                color = colors.primaryInk.copy(alpha = 0.42f),
                                radius = 84.dp.toPx(),
                                center = centerOffset,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }

                    // Layer 2: Soft-Matte Monochrome Cushion Ring (164.dp)
                    Surface(
                        modifier = Modifier
                            .size(164.dp)
                            .scale(buttonScale)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.26f),
                                spotColor = Color.Black.copy(alpha = 0.26f)
                            )
                            .border(
                                width = 1.dp,
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
                            // Layer 3: Monochrome Core Button (White when connected, Soft Dark when disconnected)
                            Surface(
                                modifier = Modifier
                                    .size(132.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onToggleConnection)
                                    .border(
                                        width = 1.2.dp,
                                        color = if (isActive) {
                                            colors.primaryInk
                                        } else {
                                            colors.border
                                        },
                                        shape = CircleShape
                                    )
                                    .testTag("vpn_connect_orb"),
                                shape = CircleShape,
                                color = coreBgColor
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Monochrome Power Glyph
                                    Canvas(modifier = Modifier.size(34.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val strokeW = 2.5.dp.toPx()
                                        val pad = w * 0.16f

                                        drawArc(
                                            color = coreContentColor,
                                            startAngle = -58f,
                                            sweepAngle = 296f,
                                            useCenter = false,
                                            topLeft = Offset(pad, pad + h * 0.04f),
                                            size = Size(w - pad * 2f, h - pad * 2f),
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                        )

                                        drawLine(
                                            color = coreContentColor,
                                            start = Offset(w / 2f, h * 0.10f),
                                            end = Offset(w / 2f, h * 0.46f),
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = when (connectionStatus) {
                                            VpnConnectionStatus.DISCONNECTED -> if (isEnglish) "CONNECT" else "ПОДКЛЮЧИТЬ"
                                            VpnConnectionStatus.CONNECTING -> if (isEnglish) "LINKING..." else "СОЕДИНЕНИЕ"
                                            VpnConnectionStatus.CONNECTED -> formatCompactTimer(connectedSeconds)
                                        },
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = 1.1.sp,
                                        color = coreContentColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Apple iOS Squircle Server Selection Card (18-20px radius, #1C1C1E, ping, chevron)
                if (activeNode != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(19.dp))
                            .iosBounceClickable(onClick = onOpenServerList)
                            .testTag("dashboard_selected_server_card"),
                        color = Color(0xFF1C1C1E),
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountryFlagSquare(
                                countryCode = activeNode.countryCode,
                                size = 26.dp,
                                cornerRadius = 7.dp,
                                isSelected = false
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEnglish) "Connected Server" else "Текущий сервер",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF8E8E93),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeNode.displayTitle(isEnglish),
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFFFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (activeNode.pingMs > 0) {
                                Surface(
                                    color = Color(0x14FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${activeNode.pingMs} ms",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (activeNode.pingMs < 80) Color(0xFF30D158) else Color(0xFFFF9F0A),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Change Server",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        },
                        onToggleFavoriteNode = onToggleFavorite
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
