package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.data.VlessServerEntity
import com.example.ui.components.MonochromeFistIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

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
    onOpenXrayConfig: (VlessServerEntity) -> Unit
) {
    val colors = LocalAstralColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Bar: Clean Back Button matching mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .border(1.dp, colors.border, CircleShape)
                    .testTag("global_nodes_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryInk,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isEnglish) "Global Nodes" else "Global Nodes",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = colors.primaryInk
        )
        Text(
            text = if (isEnglish) {
                "${allServers.size} sovereign server nodes online"
            } else {
                "${allServers.size} sovereign server nodes online"
            },
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.secondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar ("Search country, city, code...")
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = if (isEnglish) "Search country, city, code..." else "Search country, city, code...",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 14.sp,
                    color = colors.mutedText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.secondaryText
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
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryInk,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips: ALL, FAVORITES, P2P, STREAMING, REALITY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "FAVORITES", "P2P", "STREAMING")
            filters.forEach { filterKey ->
                val isSelected = selectedFilter == filterKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) colors.primaryInk else colors.card)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryInk else colors.border,
                            RoundedCornerShape(50)
                        )
                        .clickable { onFilterChange(filterKey) }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = filterKey,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = if (isSelected) colors.onPrimaryInk else colors.primaryInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("servers_lazy_list"),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                val isSelected = server.id == selectedServerId
                val isPinging = pingingServerIds.contains(server.id)
                GlobalNodeRowCard(
                    server = server,
                    isSelected = isSelected,
                    isPinging = isPinging,
                    isEnglish = isEnglish,
                    onSelect = { onSelectServer(server) },
                    onToggleFavorite = { onToggleFavorite(server) },
                    onPing = { onPingServer(server) },
                    onInspect = { onOpenXrayConfig(server) },
                    onDelete = { onDeleteServerClick(server) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
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
private fun GlobalNodeRowCard(
    server: VlessServerEntity,
    isSelected: Boolean,
    isPinging: Boolean,
    isEnglish: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPing: () -> Unit,
    onInspect: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAstralColors.current
    val loadColor = if (server.loadPercent < 30) colors.activeGreen else colors.warningOrange

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
            .testTag("server_card_${server.countryCode}_${server.id}"),
        color = colors.card,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Square Monochrome City Image
            Image(
                painter = painterResource(id = server.cityImageRes),
                contentDescription = server.city,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF18181B))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Center Title & Telemetry ("Финляндия, Хельсинки" / "HEL • 28ms • Load 14%")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${server.localizedCountry(isEnglish)}, ${server.localizedCity(isEnglish)}",
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
                                fontWeight = FontWeight.Medium,
                                color = colors.secondaryText
                            )
                        ) {
                            append("${server.nodeAirportCode} • ")
                            append(if (isPinging) "... • " else "${server.pingMs}ms • ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = loadColor
                            )
                        ) {
                            append("Load ${server.loadPercent}%")
                        }
                    },
                    fontSize = 12.sp,
                    modifier = Modifier.clickable(onClick = onPing)
                )
            }

            // Right Actions: Favorite Star & Selected Checkmark
            if (server.isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (server.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (server.isFavorite) Color(0xFFF59E0B) else colors.mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.primaryInk),
                    contentAlignment = Alignment.Center
                ) {
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
