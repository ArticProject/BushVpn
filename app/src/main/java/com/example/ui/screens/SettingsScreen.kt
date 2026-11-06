package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.VpnTunnelSettings
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settings: VpnTunnelSettings,
    onUpdateSettings: ((VpnTunnelSettings) -> VpnTunnelSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "Настройки туннеля",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Тонкая настройка VLESS REALITY, DNS, uTLS и MTU",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // DNS Servers Card
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
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DNS-резолвер туннеля (Защита от утечек DNS)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val dnsPresets = listOf(
                    Triple("1.1.1.1", "1.0.0.1", "Cloudflare DNS (Быстрый)"),
                    Triple("8.8.8.8", "8.8.4.4", "Google Public DNS"),
                    Triple("9.9.9.9", "149.112.112.112", "Quad9 Security DNS"),
                    Triple("94.140.14.14", "94.140.15.15", "AdGuard DNS (Без рекламы)")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dnsPresets.forEach { (primary, secondary, label) ->
                        val selected = settings.primaryDns == primary
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onUpdateSettings {
                                    it.copy(primaryDns = primary, secondaryDns = secondary)
                                }
                            },
                            label = { Text("$primary • $label") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }
            }
        }

        // uTLS Fingerprint & MTU Card
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
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Маскировка TLS отпечатка (uTLS Fingerprint)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Имитирует TLS ClientHello популярного браузера для обхода DPI",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("chrome", "firefox", "safari", "ios", "randomized").forEach { fp ->
                        FilterChip(
                            selected = settings.defaultFingerprint == fp,
                            onClick = { onUpdateSettings { it.copy(defaultFingerprint = fp) } },
                            label = { Text(fp.uppercase()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = NeonEmerald
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Размер пакета TUN (MTU)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1280, 1400, 1420, 1500).forEach { mtuVal ->
                        FilterChip(
                            selected = settings.mtu == mtuVal,
                            onClick = { onUpdateSettings { it.copy(mtu = mtuVal) } },
                            label = { Text("$mtuVal байт") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }
            }
        }

        // Advanced Protocol Toggles
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Безопасность и мультиплексирование",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchItem(
                    title = "Защита от разрыва (Kill Switch)",
                    subtitle = "Блокировать незашифрованный трафик при неожиданном обрыве VLESS",
                    checked = settings.killSwitchVisual,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(killSwitchVisual = v) } }
                )

                SettingSwitchItem(
                    title = "Мультиплексирование Xray MUX.Cool",
                    subtitle = "Объединять несколько TCP-потоков в одно соединение (рекомендуется для WS)",
                    checked = settings.enableMux,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(enableMux = v) } }
                )

                SettingSwitchItem(
                    title = "Поддержка маршрутизации IPv6",
                    subtitle = "Включить виртуальный стек fd00:aeth:1::2/64 внутри TUN интерфейса",
                    checked = settings.enableIpv6,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(enableIpv6 = v) } }
                )

                SettingSwitchItem(
                    title = "Автоматическое переподключение",
                    subtitle = "Восстанавливать REALITY сессию при смене Wi-Fi на мобильную сеть",
                    checked = settings.autoReconnect,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(autoReconnect = v) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
