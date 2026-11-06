package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

/**
 * Tab 2: Incy-style Servers & Subscriptions Screen (matching Screenshot_20261106_131516_771a9952.jpg):
 * - Header: "Servers" on the left + 5 compact action buttons (^, ⇅, ↻, Speedometer, +) on the right
 * - Subtitle: "${servers.size} servers · ${providerCount} providers"
 * - Search bar: "Search servers..."
 * - Protocol chips: "All", "VLESS", "Shadowsocks"
 * - Expandable Provider / Subscription Card(s) with traffic bar (∞ | --- 0.48 GB / ∞),
 *   routing notes (Wi-Fi -> Авто без БС, Мобильный / LTE -> Авто + БС), Support pill,
 *   and individual VLESS · TCP · JSON server rows with live ping.
 */
@Composable
fun ServersScreen(
    servers: List<VlessServerEntity>,
    allServers: List<VlessServerEntity>,
    selectedServerId: Long?,
    searchQuery: String,
    selectedFilter: String,
    isEnglish: Boolean,
    isPingingAll: Boolean,
    pingingServerIds: Set<Long>,
    onBackClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onSelectServer: (VlessServerEntity) -> Unit,
    onToggleFavorite: (VlessServerEntity) -> Unit,
    onPingServer: (VlessServerEntity) -> Unit,
    onPingAll: () -> Unit,
    onAddServerClick: () -> Unit,
    onDeleteServerClick: (VlessServerEntity) -> Unit,
    onOpenXrayConfig: (VlessServerEntity) -> Unit,
    onImportSubscriptionText: (String) -> Unit = {},
    onToggleEuropePack: () -> Unit = {},
    onCopiedMessage: (String) -> Unit = {}
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current

    var allExpanded by remember { mutableStateOf(true) }
    var sortByPingAsc by remember { mutableStateOf(true) }
    var selectedProtocolChip by remember { mutableStateOf("All") }

    val displayedServers = remember(servers, sortByPingAsc, selectedProtocolChip) {
        val protoFiltered = when (selectedProtocolChip) {
            "VLESS" -> servers
            "Shadowsocks" -> emptyList()
            else -> servers
        }
        if (sortByPingAsc) {
            protoFiltered.sortedBy { it.pingMs }
        } else {
            protoFiltered.sortedByDescending { it.id }
        }
    }

    val freeStandardServers = displayedServers.filter { !it.isCustom }
    val customImportedServers = displayedServers.filter { it.isCustom }
    val providerCount = (if (allServers.any { !it.isCustom }) 1 else 0) +
        (if (allServers.any { it.isCustom }) 1 else 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Incy Top Header Row: "Servers" + 5 compact action buttons (^, ⇅, ↻, Speedometer, +)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Servers",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
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
                    testTag = "incy_collapse_all_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.SwapVert,
                    contentDescription = "Sort",
                    onClick = { sortByPingAsc = !sortByPingAsc },
                    testTag = "incy_sort_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Update Subscriptions",
                    isLoading = isPingingAll,
                    onClick = onPingAll,
                    testTag = "incy_refresh_button"
                )
                IncyTopActionButton(
                    icon = Icons.Default.Speed,
                    contentDescription = "Ping All",
                    onClick = onPingAll,
                    testTag = "ping_all_servers_button"
                )
                // Highlighted "+" button to add subscription or VLESS key
                Surface(
                    modifier = Modifier
                        .width(44.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .clickable(onClick = onAddServerClick)
                        .testTag("add_vless_server_button"),
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

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Subtitle: "1 servers · 1 providers"
        Text(
            text = "${allServers.size} servers · ${providerCount.coerceAtLeast(1)} providers",
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = colors.secondaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Search Bar: "Search servers..."
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Search servers...",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 15.sp,
                    color = colors.mutedText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = colors.secondaryText
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server_search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryInk,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Protocol Filter Chips: "All", "VLESS", "Shadowsocks"
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
                        .padding(horizontal = 16.dp, vertical = 9.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Incy Provider / Subscription Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("servers_lazy_list"),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (freeStandardServers.isNotEmpty()) {
                item {
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
                        selectedServerId = selectedServerId,
                        pingingServerIds = pingingServerIds,
                        isEnglish = isEnglish,
                        forceExpanded = allExpanded,
                        onRefreshGroup = onPingAll,
                        onPingGroup = onPingAll,
                        onAddKeyToGroup = onAddServerClick,
                        onPasteFromClipboard = {
                            val clip = clipboardManager.getText()?.text?.trim().orEmpty()
                            if (clip.isNotEmpty()) {
                                onImportSubscriptionText(clip)
                            } else {
                                onAddServerClick()
                            }
                        },
                        onSelectNode = onSelectServer,
                        onPingNode = onPingServer,
                        onInspectNode = onOpenXrayConfig,
                        onDeleteNode = onDeleteServerClick,
                        onCopyNodeUri = { srv ->
                            clipboardManager.setText(AnnotatedString(VlessParser.toUri(srv)))
                            onCopiedMessage("VLESS URI скопирован")
                        }
                    )
                }
            }

            if (customImportedServers.isNotEmpty()) {
                item {
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
                        selectedServerId = selectedServerId,
                        pingingServerIds = pingingServerIds,
                        isEnglish = isEnglish,
                        forceExpanded = allExpanded,
                        onRefreshGroup = onPingAll,
                        onPingGroup = onPingAll,
                        onAddKeyToGroup = onAddServerClick,
                        onPasteFromClipboard = {
                            val clip = clipboardManager.getText()?.text?.trim().orEmpty()
                            if (clip.isNotEmpty()) {
                                onImportSubscriptionText(clip)
                            } else {
                                onAddServerClick()
                            }
                        },
                        onSelectNode = onSelectServer,
                        onPingNode = onPingServer,
                        onInspectNode = onOpenXrayConfig,
                        onDeleteNode = onDeleteServerClick,
                        onCopyNodeUri = { srv ->
                            clipboardManager.setText(AnnotatedString(VlessParser.toUri(srv)))
                            onCopiedMessage("VLESS URI скопирован")
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
internal fun IncyTopActionButton(
    icon: ImageVector,
    contentDescription: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = LocalAstralColors.current
    Surface(
        modifier = Modifier
            .width(42.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .border(1.dp, colors.border, RoundedCornerShape(11.dp))
            .testTag(testTag),
        color = colors.card,
        shape = RoundedCornerShape(11.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 2.dp,
                    color = colors.primaryInk
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = colors.primaryInk,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun IncyProviderGroupCard(
    providerTitle: String,
    lastUpdatedLabel: String,
    leftStatusLabel: String,
    trafficText: String,
    trafficProgress: Float,
    infoLines: List<String>,
    supportLabel: String?,
    timestampText: String,
    nodes: List<VlessServerEntity>,
    selectedServerId: Long?,
    pingingServerIds: Set<Long>,
    isEnglish: Boolean,
    forceExpanded: Boolean,
    onRefreshGroup: () -> Unit,
    onPingGroup: () -> Unit,
    onAddKeyToGroup: () -> Unit,
    onPasteFromClipboard: () -> Unit,
    onSelectNode: (VlessServerEntity) -> Unit,
    onPingNode: (VlessServerEntity) -> Unit,
    onInspectNode: (VlessServerEntity) -> Unit,
    onDeleteNode: (VlessServerEntity) -> Unit,
    onCopyNodeUri: (VlessServerEntity) -> Unit
) {
    val colors = LocalAstralColors.current
    var localExpanded by remember(forceExpanded) { mutableStateOf(forceExpanded) }
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
        color = colors.card,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Top Row: Chevron + Title on Left | "1h" + Refresh + Speedometer + More (...) on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { localExpanded = !localExpanded }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (localExpanded) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowUp
                        },
                        contentDescription = "Toggle group",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = providerTitle,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.primaryInk
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lastUpdatedLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )

                    IncyCircleIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Subscription",
                        onClick = onRefreshGroup
                    )
                    IncyCircleIconButton(
                        icon = Icons.Default.Speed,
                        contentDescription = "Ping Subscription Nodes",
                        onClick = onPingGroup
                    )

                    Box {
                        IncyCircleIconButton(
                            icon = Icons.Default.MoreHoriz,
                            contentDescription = "More Options",
                            onClick = { showMenu = true }
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Вставить ключ из буфера") },
                                onClick = {
                                    showMenu = false
                                    onPasteFromClipboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Добавить сервер вручную") },
                                onClick = {
                                    showMenu = false
                                    onAddKeyToGroup()
                                }
                            )
                            nodes.firstOrNull()?.let { firstNode ->
                                DropdownMenuItem(
                                    text = { Text("Скопировать vless://") },
                                    onClick = {
                                        showMenu = false
                                        onCopyNodeUri(firstNode)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Открыть X-Ray JSON") },
                                    onClick = {
                                        showMenu = false
                                        onInspectNode(firstNode)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Traffic Progress Line: "∞ | -------- 1.42 GB / ∞"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$leftStatusLabel  |",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.cardElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(trafficProgress.coerceIn(0.05f, 1f))
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.primaryInk)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = trafficText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = colors.secondaryText
                )
            }

            AnimatedVisibility(visible = localExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Provider Info Lines (matching Incy screenshot)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        infoLines.forEach { line ->
                            Text(
                                text = line,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = colors.secondaryText
                            )
                        }
                    }

                    if (supportLabel != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    nodes.firstOrNull()?.let(onInspectNode)
                                }
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp)),
                            color = colors.cardElevated,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Support",
                                    tint = colors.primaryInk,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = supportLabel,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.primaryInk
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Right-aligned timestamp & node count ("06.11.26, 13:15 · 1☰")
                    Text(
                        text = "$timestampText  ·  ${nodes.size}☰",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.mutedText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Individual Server Rows inside the Provider Card
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        nodes.forEach { server ->
                            val isSelected = server.id == selectedServerId
                            val isPinging = pingingServerIds.contains(server.id)
                            IncyServerNodeRow(
                                server = server,
                                isSelected = isSelected,
                                isPinging = isPinging,
                                isEnglish = isEnglish,
                                onSelect = { onSelectNode(server) },
                                onPing = { onPingNode(server) },
                                onInspect = { onInspectNode(server) },
                                onDelete = { onDeleteNode(server) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncyCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = LocalAstralColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(colors.cardElevated)
            .border(1.dp, colors.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.primaryInk,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun IncyServerNodeRow(
    server: VlessServerEntity,
    isSelected: Boolean,
    isPinging: Boolean,
    isEnglish: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onInspect: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAstralColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) colors.cardElevated else Color.Transparent)
            .border(
                width = if (isSelected) 1.2.dp else 0.dp,
                color = if (isSelected) colors.primaryInk else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("server_card_${server.countryCode}_${server.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Rounded Server Box Icon (matching Incy's stacked server icon box)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) colors.primaryInk else colors.cardElevated)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onInspect),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = "Server node",
                tint = if (isSelected) colors.onPrimaryInk else colors.primaryInk,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Server Title + "VLESS · TCP · JSON" subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${server.flagEmoji}  ${server.localizedCountry(isEnglish)}, ${server.localizedCity(isEnglish)}",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.primaryInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryInk
                        )
                    ) {
                        append("VLESS")
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = colors.secondaryText
                        )
                    ) {
                        append(" · ${server.networkType.uppercase()} · ")
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.secondaryText
                        )
                    ) {
                        append("JSON")
                    }
                },
                fontSize = 11.5.sp
            )
        }

        // Right Ping Readout ("28 ms")
        Text(
            text = if (isPinging) "..." else "${server.pingMs} ms",
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (server.pingMs <= 50) colors.activeGreen else colors.secondaryText,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onPing)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}
