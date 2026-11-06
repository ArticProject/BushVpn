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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VlessParser
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
    isPingingAll: Boolean = false,
    pingingServerIds: Set<Long> = emptySet(),
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
    onTriggerNoInternetDemo: () -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    val activeNode = selectedServer ?: allServers.firstOrNull()

    var allExpanded by remember { mutableStateOf(true) }
    var sortByPingAsc by remember { mutableStateOf(true) }
    var selectedProtocolChip by remember { mutableStateOf("All") }

    val sortedServers = remember(allServers, sortByPingAsc, selectedProtocolChip) {
        val base = if (selectedProtocolChip == "Shadowsocks") emptyList() else allServers
        if (sortByPingAsc) base.sortedBy { it.pingMs } else base.sortedByDescending { it.id }
    }
    val freeStandardServers = sortedServers.filter { !it.isCustom }
    val customImportedServers = sortedServers.filter { it.isCustom }
    val providerCount = (if (allServers.any { !it.isCustom }) 1 else 0) +
        (if (allServers.any { it.isCustom }) 1 else 0)

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
            .padding(horizontal = 16.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Top Hero Connection Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(228.dp)
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
                    text = if (activeNode?.isCustom == true) "SUBSCRIPTION" else "FREE STANDARD",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

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

            Box(
                modifier = Modifier
                    .size(184.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseR = 52.dp.toPx()

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
                        .size(104.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Action Pill Buttons: "• Connect Tunnel" and "+ VLESS Key"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isActiveOrLinking = connectionStatus != VpnConnectionStatus.DISCONNECTED
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
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
                    .height(46.dp)
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

        Spacer(modifier = Modifier.height(22.dp))

        // Incy-Style "Servers" Header + 5 Action Buttons directly below the connection card!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Servers",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = colors.primaryInk
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IncyTopActionButton(
                    icon = if (allExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse / Expand",
                    onClick = { allExpanded = !allExpanded },
                    testTag = "dash_incy_collapse_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.SwapVert,
                    contentDescription = "Sort",
                    onClick = { sortByPingAsc = !sortByPingAsc },
                    testTag = "dash_incy_sort_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Update Subscriptions",
                    isLoading = isPingingAll,
                    onClick = onPingAll,
                    testTag = "dash_incy_refresh_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.Speed,
                    contentDescription = "Ping All",
                    onClick = onPingAll,
                    testTag = "dash_incy_ping_button"
                )
                Surface(
                    modifier = Modifier
                        .width(42.dp)
                        .height(36.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "${allServers.size} servers · ${providerCount.coerceAtLeast(1)} providers",
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = colors.secondaryText
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Protocol Filter Chips: "All", "VLESS", "Shadowsocks"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("All", "VLESS", "Shadowsocks")
            chips.forEach { chip ->
                val isSelected = selectedProtocolChip == chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primaryInk else colors.card)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryInk else colors.border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedProtocolChip = chip }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = chip,
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) colors.onPrimaryInk else colors.primaryInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Incy Provider Cards on Tab 1
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (freeStandardServers.isNotEmpty()) {
                IncyProviderGroupCard(
                    providerTitle = "BUSH",
                    lastUpdatedLabel = "1h",
                    leftStatusLabel = "∞",
                    trafficText = "1.42 GB / ∞",
                    trafficProgress = 0.24f,
                    infoLines = listOf(
                        "✅  Бесплатный сервер по стандарту",
                        "📶  Wi-Fi → Авто без БС",
                        "📱  Мобильный / LTE → Авто + БС",
                        "ℹ️  Ping «${freeStandardServers.first().pingMs}ms» — стабильно",
                        "🌐  bushvpn.net"
                    ),
                    supportLabel = "Support",
                    timestampText = "06.11.26, 13:15",
                    nodes = freeStandardServers,
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

            if (customImportedServers.isNotEmpty()) {
                IncyProviderGroupCard(
                    providerTitle = "CUSTOM VLESS",
                    lastUpdatedLabel = "now",
                    leftStatusLabel = "∞",
                    trafficText = "0.18 GB / ∞",
                    trafficProgress = 0.12f,
                    infoLines = listOf(
                        "✅  Пользовательская подписка активна",
                        "📶  Маршрутизация VLESS Reality / XTLS"
                    ),
                    supportLabel = null,
                    timestampText = "06.11.26, 13:15",
                    nodes = customImportedServers,
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
}

private fun formatCompactTimer(totalSeconds: Long): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
