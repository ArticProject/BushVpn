package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.components.CountryFlagSquare
import com.example.ui.components.IncyPingGaugeIcon
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

/**
 * Tab 2: Incy-style Servers & Subscriptions Screen:
 * - Header: "Servers" on the left + compact action buttons (^, ⇅, ↻, Custom Ping Gauge, +) on the right
 * - Subtitle: "${allServers.size} servers · ${providerCount} providers"
 * - Search bar: "Search servers..."
 * - Protocol chips: "All", "VLESS", "Shadowsocks"
 * - Expandable Provider / Subscription Card(s) where the default BUSH free VPN card displays "Скоро"
 *   and each server row shows the Country in the left box instead of a generic server rack icon.
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

    val pinnedCount = remember(allServers) { allServers.count { it.isFavorite } }

    val displayedServers = remember(servers, sortByPingAsc, selectedProtocolChip) {
        val protoFiltered = when (selectedProtocolChip) {
            "PINNED" -> servers.filter { it.isFavorite }
            "VLESS" -> servers.filter { it.protocol.equals("VLESS", ignoreCase = true) }
            "Shadowsocks" -> servers.filter { it.protocol.equals("Shadowsocks", ignoreCase = true) }
            else -> servers
        }
        if (sortByPingAsc) {
            protoFiltered.sortedWith(compareByDescending<VlessServerEntity> { it.isFavorite }.thenBy { it.pingMs })
        } else {
            protoFiltered.sortedByDescending { it.id }
        }
    }

    val groupedProviders = remember(displayedServers) {
        displayedServers.groupBy { srv ->
            if (!srv.isCustom) "BUSH" else srv.providerName.ifBlank { "CUSTOM VLESS" }
        }
    }
    val providerCount = remember(allServers) {
        allServers.map { if (!it.isCustom) "BUSH" else it.providerName.ifBlank { "CUSTOM VLESS" } }
            .distinct()
            .size
            .coerceAtLeast(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. NoteVault Top Header Row: "Серверы / Servers" + right circular White '+' button & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isEnglish) "Servers" else "Серверы",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = colors.primaryInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isEnglish) {
                        "${allServers.size} · $pinnedCount pinned · $providerCount providers"
                    } else {
                        "${allServers.size} серв. · $pinnedCount закреп. · свайп вправо для действий"
                    },
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = colors.secondaryText
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IncyTopActionButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Update Subscriptions",
                    isLoading = isPingingAll,
                    onClick = onPingAll,
                    testTag = "incy_refresh_button"
                )
                IncyTopPingActionButton(
                    onClick = onPingAll,
                    testTag = "ping_all_servers_button"
                )
                // NoteVault Circular White '+' Button (Screenshot 2 & Video 3)
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAddServerClick)
                        .testTag("add_vless_server_button"),
                    color = colors.primaryInk,
                    shape = CircleShape
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

        Spacer(modifier = Modifier.height(12.dp))

        // 2. NoteVault Filter Pills: "Все 48", "📌 Закреплённые 1", "VLESS", "+" (Screenshot 2 & Video 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: All / Все
            val isAllSelected = selectedProtocolChip == "All"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isAllSelected) colors.cardElevated else colors.card)
                    .border(
                        1.dp,
                        if (isAllSelected) colors.primaryInk.copy(alpha = 0.45f) else colors.border,
                        RoundedCornerShape(50)
                    )
                    .clickable { selectedProtocolChip = "All" }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEnglish) "All" else "Все",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isAllSelected) colors.primaryInk else colors.secondaryText
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${allServers.size}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )
                }
            }

            // Pill 2: Pinned / Закреплённые (with orange pin icon from Video 3)
            val isPinnedSelected = selectedProtocolChip == "PINNED"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isPinnedSelected) colors.cardElevated else colors.card)
                    .border(
                        1.dp,
                        if (isPinnedSelected) Color(0xFFE87D2F) else colors.border,
                        RoundedCornerShape(50)
                    )
                    .clickable { selectedProtocolChip = "PINNED" }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = Color(0xFFE87D2F),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isEnglish) "Pinned" else "Закреплённые",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isPinnedSelected) colors.primaryInk else colors.secondaryText
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$pinnedCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )
                }
            }

            // Pill 3: VLESS
            val isVlessSelected = selectedProtocolChip == "VLESS"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isVlessSelected) colors.cardElevated else colors.card)
                    .border(
                        1.dp,
                        if (isVlessSelected) colors.primaryInk.copy(alpha = 0.45f) else colors.border,
                        RoundedCornerShape(50)
                    )
                    .clickable { selectedProtocolChip = "VLESS" }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "VLESS",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isVlessSelected) colors.primaryInk else colors.secondaryText
                )
            }

            // Pill 4: NoteVault '+' Chip
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .border(1.dp, colors.border, CircleShape)
                    .clickable(onClick = onAddServerClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = colors.secondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Search Bar: "Search servers..."
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = if (isEnglish) "Search servers..." else "Поиск серверов...",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 14.sp,
                    color = colors.mutedText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.secondaryText,
                    modifier = Modifier.size(18.dp)
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
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryInk,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Incy Provider / Subscription Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("servers_lazy_list"),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            groupedProviders.entries.forEach { (providerName, groupNodes) ->
                val isDefaultBush = providerName == "BUSH"
                item(key = providerName) {
                    IncyProviderGroupCard(
                        providerTitle = providerName,
                        lastUpdatedLabel = if (isDefaultBush) "1h" else "now",
                        leftStatusLabel = "∞",
                        trafficText = if (isDefaultBush) "1.42 GB / ∞" else "0.18 GB / ∞",
                        trafficProgress = if (isDefaultBush) 0.24f else 0.12f,
                        infoLines = if (isDefaultBush) {
                            listOf(if (isEnglish) "Soon" else "Скоро")
                        } else {
                            listOf(
                                if (isEnglish) "Active subscription" else "Активная подписка"
                            )
                        },
                        supportLabel = if (isDefaultBush) "Support" else null,
                        timestampText = "06.11.26, 13:15",
                        nodes = groupNodes,
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
                        },
                        onToggleFavoriteNode = onToggleFavorite
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
            .width(38.dp)
            .height(35.dp)
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
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
internal fun IncyTopPingActionButton(
    onClick: () -> Unit,
    testTag: String
) {
    val colors = LocalAstralColors.current
    Surface(
        modifier = Modifier
            .width(38.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .border(1.dp, colors.border, RoundedCornerShape(11.dp))
            .testTag(testTag),
        color = colors.card,
        shape = RoundedCornerShape(11.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            IncyPingGaugeIcon(
                color = colors.primaryInk,
                size = 17.dp
            )
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
    onCopyNodeUri: (VlessServerEntity) -> Unit,
    onToggleFavoriteNode: (VlessServerEntity) -> Unit = {}
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
            // Card Top Row: Chevron + Title on Left | "1h" + Refresh + Custom Ping Gauge + More (...) on Right
            val clippedTitle = remember(providerTitle) {
                if (providerTitle.length > 20) providerTitle.take(20).trimEnd() + "…" else providerTitle
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
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
                        text = clippedTitle,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        color = colors.primaryInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lastUpdatedLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.5.sp,
                        color = colors.mutedText,
                        maxLines = 1,
                        softWrap = false
                    )

                    IncyCircleIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Subscription",
                        onClick = onRefreshGroup
                    )
                    IncyCirclePingButton(
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
                                text = { Text("Вставить подписку / ключ из буфера") },
                                onClick = {
                                    showMenu = false
                                    onPasteFromClipboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Добавить подписку или сервер") },
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
                                if (firstNode.isCustom) {
                                    DropdownMenuItem(
                                        text = { Text("Удалить подписку ($providerTitle)") },
                                        onClick = {
                                            showMenu = false
                                            nodes.forEach { onDeleteNode(it) }
                                        }
                                    )
                                }
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

                    // Provider Info Lines ("Скоро" for BUSH free VPN)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        infoLines.forEach { line ->
                            Text(
                                text = line,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
                                lineHeight = 20.sp,
                                color = colors.primaryInk
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

                    // Individual Server Rows inside the Provider Card (NoteVault Swipe-to-Action Cards)
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
                                onDelete = { onDeleteNode(server) },
                                onTogglePin = { onToggleFavoriteNode(server) },
                                onShareUri = { onCopyNodeUri(server) }
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
private fun IncyCirclePingButton(
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
        IncyPingGaugeIcon(
            color = colors.primaryInk,
            size = 16.dp
        )
    }
}

/**
 * NoteVault-Style Swipeable Server Card (1:1 with Video 3 & Screenshot 2):
 * - Swiping the card right reveals the 3 circular action buttons on the left:
 *   1. Orange Pin Circle (#E87D2F) -> pins/unpins the server to "Pinned / Закреплённые"
 *   2. Dark-Grey Share Circle (#262832) -> copies/shares the vless:// key
 *   3. Red Delete Circle (#E53935) -> deletes the server node
 * - Displays an orange pin badge next to the title when pinned!
 */
@Composable
private fun IncyServerNodeRow(
    server: VlessServerEntity,
    isSelected: Boolean,
    isPinging: Boolean,
    isEnglish: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onInspect: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit = {},
    onShareUri: () -> Unit = {}
) {
    val colors = LocalAstralColors.current
    var isActionRevealed by remember { mutableStateOf(false) }
    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    val cardOffsetX by animateDpAsState(
        targetValue = if (isActionRevealed) 146.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "notevault_swipe_offset"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        // Underlying 3 Circular Swipe Actions (Orange Pin, Grey Share, Red Delete — Video 3 @ 0:10-0:20)
        Row(
            modifier = Modifier.padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Orange Pin Circle (#E87D2F)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE87D2F))
                    .clickable {
                        onTogglePin()
                        isActionRevealed = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pin",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 2. Dark-Grey Share Circle (#262832)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262832))
                    .border(1.dp, colors.border, CircleShape)
                    .clickable {
                        onShareUri()
                        isActionRevealed = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            // 3. Red Delete Circle (#E53935)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable {
                        isActionRevealed = false
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Foreground NoteVault Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = cardOffsetX)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) colors.cardElevated else colors.surface)
                .border(
                    width = if (isSelected) 1.3.dp else 1.dp,
                    color = if (isSelected) colors.primaryInk else colors.border,
                    shape = RoundedCornerShape(18.dp)
                )
                .pointerInput(server.id) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulated = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulated += dragAmount
                            if (dragAccumulated > 36f) {
                                isActionRevealed = true
                            } else if (dragAccumulated < -28f) {
                                isActionRevealed = false
                            }
                        }
                    )
                }
                .clickable {
                    if (isActionRevealed) {
                        isActionRevealed = false
                    } else {
                        onSelect()
                    }
                }
                .padding(horizontal = 12.dp, vertical = 11.dp)
                .testTag("server_card_${server.countryCode}_${server.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Country Square: painted in the real flag colors of the country, no click action
            CountryFlagSquare(
                countryCode = server.countryCode,
                size = 42.dp,
                cornerRadius = 12.dp,
                isSelected = isSelected
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center Server Title + Orange Pin Indicator + "VLESS · TCP · JSON" subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (server.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFE87D2F),
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 3.dp)
                        )
                    }
                    Text(
                        text = server.displayTitle(isEnglish),
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = colors.primaryInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                            append(server.protocol.uppercase())
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
                            append(if (server.isCustom) server.host else "JSON")
                        }
                    },
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Live TCP Latency Readout + NoteVault Chevron `>`
            if (server.countryCode != "INFO") {
                val pingColor = when {
                    isPinging -> colors.secondaryText
                    server.pingMs in 1..85 -> colors.activeGreen
                    server.pingMs in 86..190 -> Color(0xFFF59E0B)
                    server.pingMs > 190 -> Color(0xFFEF4444)
                    else -> Color(0xFFEF4444).copy(alpha = 0.78f)
                }
                val pingLabel = when {
                    isPinging -> "•••"
                    server.pingMs > 0 -> "${server.pingMs} ms"
                    else -> if (isEnglish) "timeout" else "таймаут"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onPing)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.6.dp,
                            color = colors.primaryInk
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = pingLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = pingColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Actions",
                        tint = colors.mutedText,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { isActionRevealed = !isActionRevealed }
                    )
                }
            }
        }
    }
}
