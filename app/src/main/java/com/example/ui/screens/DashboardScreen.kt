package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VlessServerEntity
import com.example.ui.VpnConnectionStatus
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
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleConnection: () -> Unit,
    onSelectServer: (VlessServerEntity) -> Unit,
    onOpenServerList: () -> Unit,
    onOpenAddServer: () -> Unit,
    onOpenXrayConfig: (VlessServerEntity) -> Unit,
    onDeleteServer: (VlessServerEntity) -> Unit = {},
    onTriggerNoInternetDemo: () -> Unit
) {
    val colors = LocalAstralColors.current
    val activeNode = selectedServer ?: allServers.firstOrNull()

    val infiniteTransition = rememberInfiniteTransition(label = "hero_linking_pulse")
    val ringScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.46f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringScale1"
    )
    val ringAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha1"
    )
    val ringScale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringScale2"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Header Bar: Monochrome Fist + BUSH VPN on left | HEL pill + RU/EN + Offline Demo + Theme button on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonochromeFistIcon(
                    color = colors.primaryInk,
                    size = 22.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BUSH VPN",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.primaryInk
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (activeNode != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.card)
                            .border(1.dp, colors.border, RoundedCornerShape(50))
                            .clickable(onClick = onOpenServerList)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("header_node_code_pill")
                    ) {
                        Text(
                            text = activeNode.nodeAirportCode,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp,
                            color = colors.primaryInk
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .clickable(onClick = onToggleLanguage)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("header_lang_toggle")
                ) {
                    Text(
                        text = if (isEnglish) "EN" else "RU",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.primaryInk
                    )
                }

                IconButton(
                    onClick = onTriggerNoInternetDemo,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.card)
                        .border(1.dp, colors.border, CircleShape)
                        .testTag("header_offline_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No internet animation",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Monochrome Architectural City Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(234.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141416))
                .border(1.dp, colors.border, RoundedCornerShape(28.dp))
                .clickable(onClick = onToggleConnection)
                .testTag("vpn_connect_orb")
        ) {
            Image(
                painter = painterResource(
                    id = activeNode?.cityImageRes ?: R.drawable.img_city_helsinki_bw_1793947042162
                ),
                contentDescription = activeNode?.city ?: "City node",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.42f),
                                Color.Black.copy(alpha = 0.68f)
                            )
                        )
                    )
            )

            // Top-Left Free / Subscription Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (activeNode?.isCustom == true) "SUBSCRIPTION NODE" else "FREE STANDARD",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            // Top-Right Ping Pill "• 28ms"
            if (activeNode != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.62f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (connectionStatus == VpnConnectionStatus.CONNECTED) {
                                    Color(0xFF34D399)
                                } else {
                                    Color.White.copy(alpha = 0.75f)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activeNode.pingMs}ms",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            // Center Circular Power Button with 60fps Pulsing Rings (CONNECT / LINKING / ACTIVE)
            Box(
                modifier = Modifier
                    .size(188.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseR = 53.dp.toPx()

                    if (connectionStatus == VpnConnectionStatus.DISCONNECTED) {
                        drawCircle(
                            color = Color.White.copy(alpha = ringAlpha1 * 0.42f),
                            radius = baseR * (1f + (ringScale1 - 1f) * 0.55f),
                            center = center,
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    } else {
                        drawCircle(
                            color = Color.White.copy(alpha = ringAlpha1 * 0.65f),
                            radius = baseR * ringScale1,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = ringAlpha1 * 0.85f),
                            radius = baseR * ringScale2,
                            center = center,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionStatus) {
                                VpnConnectionStatus.CONNECTED -> Color.White.copy(alpha = 0.24f)
                                VpnConnectionStatus.CONNECTING -> Color.White.copy(alpha = 0.20f)
                                VpnConnectionStatus.DISCONNECTED -> Color.Black.copy(alpha = 0.45f)
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(
                                alpha = if (connectionStatus == VpnConnectionStatus.DISCONNECTED) 0.35f else 0.75f
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Connect",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (connectionStatus) {
                                VpnConnectionStatus.DISCONNECTED -> "CONNECT"
                                VpnConnectionStatus.CONNECTING -> "LINKING"
                                VpnConnectionStatus.CONNECTED -> formatCompactTimer(connectedSeconds)
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.4.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dual Action Pill Buttons: "• Connect Tunnel" and "+ VLESS Key" (Happ / Incy style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isActiveOrLinking = connectionStatus != VpnConnectionStatus.DISCONNECTED
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleConnection)
                    .border(
                        1.dp,
                        if (isActiveOrLinking) colors.primaryInk else colors.border,
                        RoundedCornerShape(50)
                    )
                    .testTag("connect_tunnel_pill_button"),
                color = if (isActiveOrLinking) colors.primaryInk else colors.cardElevated,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActiveOrLinking) colors.activeGreen else colors.primaryInk
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (connectionStatus) {
                            VpnConnectionStatus.DISCONNECTED -> if (isEnglish) "Connect Tunnel" else "Подключить"
                            VpnConnectionStatus.CONNECTING -> if (isEnglish) "Linking..." else "Подключение..."
                            VpnConnectionStatus.CONNECTED -> if (isEnglish) "Disconnect" else "Отключить"
                        },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isActiveOrLinking) colors.onPrimaryInk else colors.primaryInk
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenAddServer)
                    .border(1.dp, colors.border, RoundedCornerShape(50))
                    .testTag("inspect_node_pill_button"),
                color = colors.cardElevated,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add VLESS",
                        tint = colors.primaryInk,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "Import VLESS" else "Ключ vless://",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.primaryInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tab 1 Servers List Section (Happ / Incy style: Free Standard Server + any imported subscription nodes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = if (isEnglish) "ROUTING NODES" else "СЕРВЕРЫ ТУННЕЛЯ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isEnglish) "Available Servers" else "Доступные серверы",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    color = colors.primaryInk
                )
            }

            Text(
                text = if (isEnglish) "Subscriptions →" else "Подписки →",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.primaryInk,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenServerList)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            allServers.forEach { node ->
                HappNodeRowCard(
                    node = node,
                    isSelected = node.id == activeNode?.id,
                    isEnglish = isEnglish,
                    onSelect = { onSelectServer(node) },
                    onInspect = { onOpenXrayConfig(node) },
                    onDelete = { onDeleteServer(node) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

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
}

@Composable
private fun HappNodeRowCard(
    node: VlessServerEntity,
    isSelected: Boolean,
    isEnglish: Boolean,
    onSelect: () -> Unit,
    onInspect: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAstralColors.current
    val loadColor = if (node.loadPercent < 30) colors.activeGreen else colors.warningOrange
    val isDefaultFree = !node.isCustom

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primaryInk else colors.border,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("featured_node_${node.countryCode}"),
        color = colors.card,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = node.cityImageRes),
                contentDescription = node.city,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF18181B))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${node.localizedCountry(isEnglish)}, ${node.localizedCity(isEnglish)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.primaryInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isDefaultFree) colors.primaryInk else colors.cardElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDefaultFree) "FREE" else "SUB",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.6.sp,
                            color = if (isDefaultFree) colors.onPrimaryInk else colors.primaryInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = colors.secondaryText
                            )
                        ) {
                            append("${node.nodeAirportCode} • ${node.pingMs}ms • ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = loadColor
                            )
                        ) {
                            append("VLESS Reality")
                        }
                    },
                    fontSize = 12.sp,
                    modifier = Modifier.clickable(onClick = onInspect)
                )
            }

            if (node.isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.primaryInk else colors.cardElevated)
                    .border(1.dp, colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = colors.onPrimaryInk,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatCompactTimer(totalSeconds: Long): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
