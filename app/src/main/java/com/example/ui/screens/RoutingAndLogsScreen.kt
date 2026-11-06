package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.data.RoutingMode
import com.example.data.VlessServerEntity
import com.example.data.VpnTunnelSettings
import com.example.ui.CoreLogEntry
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoutingAndLogsScreen(
    settings: VpnTunnelSettings,
    selectedServer: VlessServerEntity?,
    logs: List<CoreLogEntry>,
    onUpdateSettings: ((VpnTunnelSettings) -> VpnTunnelSettings) -> Unit,
    onClearLogs: () -> Unit,
    onOpenXrayConfig: (VlessServerEntity) -> Unit,
    onCopiedMessage: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Маршрутизация и Ядро",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Правила GeoIP / GeoSite и телеметрия Xray-VLESS",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (selectedServer != null) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenXrayConfig(selectedServer) }
                            .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                        color = ElectricCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "JSON Конфиг",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JSON",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // Routing Mode Selection
        item {
            Text(
                text = "РЕЖИМ МАРШРУТИЗАЦИИ ТРАФИКА",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingMode.entries.forEach { mode ->
                    val isSelected = settings.routingMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onUpdateSettings {
                                    it.copy(
                                        routingMode = mode,
                                        bypassRuDomains = mode == RoutingMode.RU_BYPASS
                                    )
                                }
                            }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ElectricCyan else ObsidianBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("routing_mode_${mode.name}"),
                        color = if (isSelected) ObsidianCardElevated else ObsidianCard,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Split Tunneling & Filter Rules
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp)),
                color = ObsidianCard,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Правила фильтрации пакетов",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ToggleRuleRow(
                        title = "Прямой доступ к сайтам .ru / .рф (GeoSite RU)",
                        subtitle = "Госуслуги, банки и российские сервисы работают без проксирования",
                        checked = settings.bypassRuDomains,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(bypassRuDomains = checked) }
                        }
                    )

                    ToggleRuleRow(
                        title = "Блокировка рекламы и трекеров (geosite:ads)",
                        subtitle = "Фильтрация рекламных доменов и телеметрии на уровне ядра Xray",
                        checked = settings.blockAdsAndTrackers,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(blockAdsAndTrackers = checked) }
                        }
                    )

                    ToggleRuleRow(
                        title = "Обход локальной сети LAN (192.168.0.0/16)",
                        subtitle = "Доступ к домашнему роутеру, NAS и принтерам напрямую",
                        checked = settings.bypassLan,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(bypassLan = checked) }
                        }
                    )
                }
            }
        }

        // Live Xray-Core Terminal Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ЖУРНАЛ СОБЫТИЙ XRAY-CORE",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonEmerald
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val joined = logs.joinToString("\n") { "[${it.timestamp}] [${it.level}] ${it.message}" }
                            clipboardManager.setText(AnnotatedString(joined))
                            onCopiedMessage("Журнал Xray скопирован в буфер")
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Копировать лог",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Очистить лог",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        items(logs, key = { it.id }) { entry ->
            val badgeColor = when (entry.level) {
                "REALITY" -> NeonEmerald
                "ROUTE" -> ElectricCyan
                "DNS" -> Color(0xFFA78BFA)
                "WARN" -> AmberWarning
                else -> TextSecondary
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.level,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = badgeColor,
                    modifier = Modifier.width(56.dp)
                )
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleRuleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBackground,
                checkedTrackColor = ElectricCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianBackground
            )
        )
    }
}
