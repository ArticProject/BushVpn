package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RoutingMode
import com.example.data.VpnTunnelSettings
import com.example.ui.CoreLogEntry
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

private enum class HappPickerSheet {
    NONE,
    ROUTING,
    PER_APP_PROXY,
    PING_TYPE,
    PING_URL,
    FRAGMENT_PRESET,
    DNS_SERVER,
    MTU_VALUE,
    UPDATE_INTERVAL,
    USER_AGENT,
    FINGERPRINT,
    CORE_LOGS
}

/**
 * 1:1 Happ Proxy Utility Settings Screen (Настройки Happ):
 * Full grouped settings cards for Subscriptions, Routing, Per-App Proxy, Ping (TCP / Via Proxy HEAD / ICMP),
 * Fragmentation (DPI/TSPU bypass), Mux, Sniffing, DNS / FakeDNS, Appearance, and Xray-core Logs.
 */
@Composable
fun HappSettingsScreen(
    settings: VpnTunnelSettings,
    coreLogs: List<CoreLogEntry>,
    isEnglish: Boolean,
    isDarkTheme: Boolean,
    onUpdateSettings: ((VpnTunnelSettings) -> VpnTunnelSettings) -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onClearLogs: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onCopiedMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    var activePicker by remember { mutableStateOf(HappPickerSheet.NONE) }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top Bar: Back Button + "Настройки" (Happ style header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.card)
                        .border(1.dp, colors.border, CircleShape)
                        .testTag("happ_settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryInk,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isEnglish) "Settings" else "Настройки",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = colors.primaryInk
                    )
                    Text(
                        text = "Happ • Xray-core v1.8.24",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.secondaryText
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { activePicker = HappPickerSheet.CORE_LOGS }
                    .border(1.dp, colors.border, RoundedCornerShape(50)),
                color = colors.card,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Logs",
                        tint = colors.primaryInk,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "Logs" else "Логи",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.primaryInk
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: ПОДПИСКИ (Subscriptions)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "SUBSCRIPTIONS" else "ПОДПИСКИ"
                ) {
                    HappSwitchRow(
                        icon = Icons.Default.Refresh,
                        title = if (isEnglish) "Auto-update subscriptions" else "Автообновление подписок",
                        subtitle = if (isEnglish) "Update subscription nodes in background" else "Фоновое обновление списков серверов",
                        checked = settings.autoUpdateSubs,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(autoUpdateSubs = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Schedule,
                        title = if (isEnglish) "Update interval" else "Интервал обновления",
                        value = "${settings.updateIntervalHours} ч",
                        onClick = { activePicker = HappPickerSheet.UPDATE_INTERVAL }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.SyncAlt,
                        title = if (isEnglish) "Update on app launch" else "Обновлять при открытии",
                        subtitle = if (isEnglish) "Fetch latest nodes on startup" else "Запрашивать свежие узлы при старте",
                        checked = settings.updateOnOpen,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(updateOnOpen = checked) }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Speed,
                        title = if (isEnglish) "Ping on launch" else "Пинг при открытии",
                        subtitle = if (isEnglish) "Automatically test latency on open" else "Измерять задержку серверов при входе",
                        checked = settings.pingOnOpen,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(pingOnOpen = checked) }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.UnfoldLess,
                        title = if (isEnglish) "Collapse subscriptions" else "Сворачивать подписки",
                        subtitle = if (isEnglish) "Compact provider cards by default" else "Компактный вид групп по умолчанию",
                        checked = settings.collapseSubsByDefault,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(collapseSubsByDefault = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Http,
                        title = "User-Agent",
                        value = settings.subscriptionUserAgent.substringBefore(" "),
                        onClick = { activePicker = HappPickerSheet.USER_AGENT }
                    )
                }
            }

            // SECTION 2: МАРШРУТИЗАЦИЯ И ТУННЕЛЬ (Routing & Tunnel)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "ROUTING & TUNNEL" else "МАРШРУТИЗАЦИЯ И ТУННЕЛЬ"
                ) {
                    HappValueRow(
                        icon = Icons.Default.Route,
                        title = if (isEnglish) "Routing mode" else "Правила маршрутизации",
                        value = when (settings.routingMode) {
                            RoutingMode.ALL_TRAFFIC -> "Global"
                            RoutingMode.RU_BYPASS -> "RU Bypass (.ru)"
                            RoutingMode.LOW_LATENCY_SPLIT -> "Rule-based"
                        },
                        onClick = { activePicker = HappPickerSheet.ROUTING }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Apps,
                        title = if (isEnglish) "Per-App Proxy" else "Выбор приложений (Per-App)",
                        value = settings.perAppProxyMode,
                        onClick = { activePicker = HappPickerSheet.PER_APP_PROXY }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Public,
                        title = if (isEnglish) "Bypass RU domains (.ru / .рф)" else "Обход доменов РФ (.ru / .рф)",
                        subtitle = if (isEnglish) "Direct connection for Russian services & banks" else "Банки, Госуслуги и .ru сайты напрямую без VPN",
                        checked = settings.bypassRuDomains,
                        onCheckedChange = { checked ->
                            onUpdateSettings {
                                it.copy(
                                    bypassRuDomains = checked,
                                    routingMode = if (checked) RoutingMode.RU_BYPASS else RoutingMode.ALL_TRAFFIC
                                )
                            }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Layers,
                        title = if (isEnglish) "Bypass LAN" else "Обход локальной сети (LAN)",
                        subtitle = "192.168.0.0/16, 10.0.0.0/8 → Direct",
                        checked = settings.bypassLan,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(bypassLan = checked) }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Shield,
                        title = if (isEnglish) "Block Ads & Trackers" else "Блокировка рекламы (geosite:ads)",
                        subtitle = if (isEnglish) "Drop ad & telemetry domains in Xray routing" else "Блокировать рекламные сети на уровне роутинга",
                        checked = settings.blockAdsAndTrackers,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(blockAdsAndTrackers = checked) }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Code,
                        title = "Sniffing (http, tls, quic)",
                        subtitle = if (isEnglish) "Domain override for accurate routing" else "Определение доменов из TLS/HTTP заголовков",
                        checked = settings.enableSniffing,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableSniffing = checked) }
                        }
                    )
                }
            }

            // SECTION 3: ПИНГ И ПРОВЕРКА СОЕДИНЕНИЯ (Ping Settings)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "PING & LATENCY TEST" else "НАСТРОЙКИ ПИНГА"
                ) {
                    HappValueRow(
                        icon = Icons.Default.Speed,
                        title = if (isEnglish) "Ping type" else "Тип проверки пинга",
                        value = settings.pingType,
                        onClick = { activePicker = HappPickerSheet.PING_TYPE }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Link,
                        title = if (isEnglish) "Test URL" else "URL проверки (204)",
                        value = settings.pingUrl.removePrefix("https://"),
                        onClick = { activePicker = HappPickerSheet.PING_URL }
                    )
                }
            }

            // SECTION 4: ФРАГМЕНТАЦИЯ И ОБХОД DPI / ТСПУ (Fragment & Mux)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "FRAGMENTATION & MUX (DPI BYPASS)" else "ФРАГМЕНТАЦИЯ И ОБХОД ТСПУ"
                ) {
                    HappSwitchRow(
                        icon = Icons.Default.Bolt,
                        title = if (isEnglish) "TLS Fragmentation" else "Фрагментация пакетов (Fragment)",
                        subtitle = if (isEnglish) "Split TLS ClientHello to bypass DPI firewalls" else "Дробление TLS ClientHello для обхода блокировок",
                        checked = settings.enableFragment,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableFragment = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Tune,
                        title = if (isEnglish) "Fragment parameters" else "Параметры фрагментации",
                        value = "${settings.fragmentPackets} • ${settings.fragmentLength}B • ${settings.fragmentInterval}ms",
                        onClick = { activePicker = HappPickerSheet.FRAGMENT_PRESET }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.SyncAlt,
                        title = "Mux (Multiplexing)",
                        subtitle = if (isEnglish) "Multiplex TCP streams (concurrency: ${settings.muxConcurrency})" else "Мультиплексирование потоков (concurrency: ${settings.muxConcurrency})",
                        checked = settings.enableMux,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableMux = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Fingerprint,
                        title = "uTLS Fingerprint",
                        value = settings.defaultFingerprint,
                        onClick = { activePicker = HappPickerSheet.FINGERPRINT }
                    )
                }
            }

            // SECTION 5: DNS И СЕТЬ (DNS & Network)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "DNS & NETWORK" else "DNS И СЕТЬ"
                ) {
                    HappValueRow(
                        icon = Icons.Default.Dns,
                        title = if (isEnglish) "Upstream DNS" else "DNS серверы",
                        value = "${settings.primaryDns} / ${settings.secondaryDns}",
                        onClick = { activePicker = HappPickerSheet.DNS_SERVER }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Security,
                        title = "FakeDNS",
                        subtitle = if (isEnglish) "Accelerate handshake via virtual DNS pool" else "Ускорение подключения через пул FakeIP",
                        checked = settings.enableFakeDns,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableFakeDns = checked) }
                        }
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Public,
                        title = "IPv6",
                        subtitle = if (isEnglish) "Route IPv6 traffic inside tunnel" else "Поддержка IPv6 внутри туннеля",
                        checked = settings.enableIpv6,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableIpv6 = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Tune,
                        title = "MTU",
                        value = "${settings.mtu}",
                        onClick = { activePicker = HappPickerSheet.MTU_VALUE }
                    )
                }
            }

            // SECTION 6: ОФОРМЛЕНИЕ И СИСТЕМА (Appearance & System)
            item {
                HappSettingsGroupCard(
                    header = if (isEnglish) "APPEARANCE & SYSTEM" else "ОФОРМЛЕНИЕ И СИСТЕМА"
                ) {
                    HappSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = if (isEnglish) "Dark theme" else "Тёмная тема",
                        subtitle = if (isEnglish) "High-contrast monochrome dark mode" else "Монохромная тёмная тема оформления",
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Language,
                        title = if (isEnglish) "Language" else "Язык интерфейса",
                        value = if (isEnglish) "English (EN)" else "Русский (RU)",
                        onClick = onToggleLanguage
                    )
                    HappDivider()
                    HappSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = if (isEnglish) "Speed in notification" else "Скорость в уведомлении",
                        subtitle = if (isEnglish) "Show real-time traffic in status bar" else "Показывать живой трафик в шторке Android",
                        checked = settings.showSpeedNotification,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(showSpeedNotification = checked) }
                        }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Terminal,
                        title = if (isEnglish) "Xray-core logs" else "Журнал ядра Xray-core",
                        value = "${coreLogs.size} записей",
                        onClick = { activePicker = HappPickerSheet.CORE_LOGS }
                    )
                    HappDivider()
                    HappValueRow(
                        icon = Icons.Default.Refresh,
                        title = if (isEnglish) "Welcome onboarding" else "Экран приветствия",
                        value = if (isEnglish) "Show" else "Показать",
                        onClick = onReplayOnboarding
                    )
                }
            }
        }
    }

    // Sub-Pickers for Happ Settings
    when (activePicker) {
        HappPickerSheet.ROUTING -> {
            HappOptionPickerDialog(
                title = if (isEnglish) "Routing Mode" else "Правила маршрутизации",
                options = RoutingMode.entries.map { it.title to it.subtitle },
                selectedIndex = RoutingMode.entries.indexOf(settings.routingMode),
                onSelectIndex = { idx ->
                    val mode = RoutingMode.entries[idx]
                    onUpdateSettings {
                        it.copy(
                            routingMode = mode,
                            bypassRuDomains = mode == RoutingMode.RU_BYPASS
                        )
                    }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.PER_APP_PROXY -> {
            val modes = listOf(
                "Все приложения" to "Туннелировать весь трафик устройства",
                "Обход банков РФ и Госуслуг" to "Сбербанк, Т-Банк, ВТБ, Госуслуги и Яндекс работают напрямую",
                "Только выбранные (Браузеры / Telegram)" to "Через VPN работают только мессенджеры и браузеры"
            )
            HappOptionPickerDialog(
                title = if (isEnglish) "Per-App Proxy" else "Проксирование приложений",
                options = modes,
                selectedIndex = modes.indexOfFirst { it.first == settings.perAppProxyMode }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(perAppProxyMode = modes[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.PING_TYPE -> {
            val types = listOf(
                "TCP" to "Быстрое измерение TCP-рукопожатия до узла (Рекомендуется)",
                "Via Proxy (HEAD)" to "Реальный HTTP HEAD запрос через Xray-туннель (204)",
                "ICMP" to "Системный эхо-запрос до IP-адреса сервера"
            )
            HappOptionPickerDialog(
                title = if (isEnglish) "Ping Type" else "Тип проверки пинга",
                options = types,
                selectedIndex = types.indexOfFirst { it.first == settings.pingType }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(pingType = types[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.PING_URL -> {
            val urls = listOf(
                "https://cp.cloudflare.com/generate_204" to "Cloudflare 204 (По стандарту в Happ)",
                "https://www.gstatic.com/generate_204" to "Google Gstatic 204",
                "https://captive.apple.com/hotspot-detect.html" to "Apple Captive Portal"
            )
            HappOptionPickerDialog(
                title = if (isEnglish) "Connection Test URL" else "URL проверки соединения",
                options = urls,
                selectedIndex = urls.indexOfFirst { it.first == settings.pingUrl }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(pingUrl = urls[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.FRAGMENT_PRESET -> {
            val presets = listOf(
                Triple("tlshello", "10-30", "10-20") to "Стандарт Happ (tlshello • 10-30 байт • 10-20 мс)",
                Triple("tlshello", "5-15", "5-10") to "Агрессивный обход ТСПУ (tlshello • 5-15 байт • 5-10 мс)",
                Triple("1-3", "20-50", "15-30") to "Пакеты 1-3 (20-50 байт • 15-30 мс)"
            )
            HappOptionPickerDialog(
                title = if (isEnglish) "Fragment Parameters" else "Параметры фрагментации",
                options = presets.map { "${it.first.first} (${it.first.second}B)" to it.second },
                selectedIndex = presets.indexOfFirst { it.first.second == settings.fragmentLength }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    val chosen = presets[idx].first
                    onUpdateSettings {
                        it.copy(
                            enableFragment = true,
                            fragmentPackets = chosen.first,
                            fragmentLength = chosen.second,
                            fragmentInterval = chosen.third
                        )
                    }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.DNS_SERVER -> {
            val dnsList = listOf(
                ("1.1.1.1" to "1.0.0.1") to "Cloudflare DNS (1.1.1.1 / 1.0.0.1)",
                ("8.8.8.8" to "8.8.4.4") to "Google Public DNS (8.8.8.8 / 8.8.4.4)",
                ("9.9.9.9" to "149.112.112.112") to "Quad9 Secure DNS (9.9.9.9)",
                ("77.88.8.8" to "77.88.8.1") to "Yandex DNS (77.88.8.8)"
            )
            HappOptionPickerDialog(
                title = if (isEnglish) "DNS Servers" else "DNS серверы",
                options = dnsList.map { "${it.first.first} / ${it.first.second}" to it.second },
                selectedIndex = dnsList.indexOfFirst { it.first.first == settings.primaryDns }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    val pair = dnsList[idx].first
                    onUpdateSettings { it.copy(primaryDns = pair.first, secondaryDns = pair.second) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.MTU_VALUE -> {
            val mtus = listOf(
                1400 to "1400 (Оптимально для мобильных сетей и LTE)",
                1420 to "1420 (Стандарт WireGuard / Xray)",
                1500 to "1500 (Максимум для домашнего Wi-Fi)",
                1280 to "1280 (Минимальный IPv6 без фрагментации)"
            )
            HappOptionPickerDialog(
                title = "MTU",
                options = mtus.map { it.first.toString() to it.second },
                selectedIndex = mtus.indexOfFirst { it.first == settings.mtu }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(mtu = mtus[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.UPDATE_INTERVAL -> {
            val hours = listOf(1, 3, 6, 12, 24)
            HappOptionPickerDialog(
                title = if (isEnglish) "Auto-Update Interval" else "Интервал обновления подписок",
                options = hours.map { "$it ч" to "Обновлять подписки каждые $it ч." },
                selectedIndex = hours.indexOf(settings.updateIntervalHours).coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(updateIntervalHours = hours[idx]) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.USER_AGENT -> {
            val agents = listOf(
                "Happ/1.4 (Android; Xray-core)" to "Happ Proxy Utility (По умолчанию)",
                "Incy/1.4 (Android; Xray-core)" to "Incy Android Client",
                "v2rayNG/1.8.24" to "v2rayNG (Совместимый профиль)",
                "ClashMetaForAndroid/2.10" to "Clash.Meta"
            )
            HappOptionPickerDialog(
                title = "User-Agent подписок",
                options = agents,
                selectedIndex = agents.indexOfFirst { it.first == settings.subscriptionUserAgent }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(subscriptionUserAgent = agents[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.FINGERPRINT -> {
            val fps = listOf(
                "chrome" to "Chrome uTLS (Рекомендуется для Reality)",
                "firefox" to "Firefox uTLS",
                "safari" to "Safari / iOS uTLS",
                "randomized" to "Случайный отпечаток TLS при каждом соединении"
            )
            HappOptionPickerDialog(
                title = "uTLS Fingerprint",
                options = fps,
                selectedIndex = fps.indexOfFirst { it.first == settings.defaultFingerprint }.coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onUpdateSettings { it.copy(defaultFingerprint = fps[idx].first) }
                    activePicker = HappPickerSheet.NONE
                },
                onDismiss = { activePicker = HappPickerSheet.NONE }
            )
        }

        HappPickerSheet.CORE_LOGS -> {
            Dialog(
                onDismissRequest = { activePicker = HappPickerSheet.NONE },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .heightIn(max = 580.dp)
                        .border(1.dp, colors.border, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    color = colors.card
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Xray-core Logs",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = colors.primaryInk
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        val dump = coreLogs.joinToString("\n") {
                                            "[${it.timestamp}] [${it.level}] ${it.message}"
                                        }
                                        clipboardManager.setText(AnnotatedString(dump))
                                        onCopiedMessage("Логи скопированы")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = colors.primaryInk,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = onClearLogs) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Clear",
                                        tint = colors.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { activePicker = HappPickerSheet.NONE }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = colors.secondaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .heightIn(min = 240.dp, max = 440.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (coreLogs.isEmpty()) {
                                    Text(
                                        text = "[INFO] Xray-core ready",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = colors.secondaryText
                                    )
                                } else {
                                    coreLogs.forEach { entry ->
                                        Text(
                                            text = "${entry.timestamp} [${entry.level}] ${entry.message}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.primaryInk
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HappPickerSheet.NONE -> Unit
    }
}

@Composable
private fun HappSettingsGroupCard(
    header: String,
    content: @Composable () -> Unit
) {
    val colors = LocalAstralColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = header,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 1.2.sp,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(22.dp)),
            color = colors.card,
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun HappDivider() {
    val colors = LocalAstralColors.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp, end = 16.dp),
        thickness = 0.7.dp,
        color = colors.border
    )
}

@Composable
private fun HappSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalAstralColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.cardElevated)
                .border(1.dp, colors.border, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryInk,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                color = colors.primaryInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.secondaryText
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimaryInk,
                checkedTrackColor = colors.primaryInk,
                uncheckedThumbColor = colors.secondaryText,
                uncheckedTrackColor = colors.cardElevated
            )
        )
    }
}

@Composable
private fun HappValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val colors = LocalAstralColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.cardElevated)
                .border(1.dp, colors.border, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryInk,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
            color = colors.primaryInk,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(132.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.mutedText,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HappOptionPickerDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAstralColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, colors.border, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = colors.primaryInk
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.secondaryText
                        )
                    }
                }

                options.forEachIndexed { idx, (label, desc) ->
                    val isSelected = idx == selectedIndex
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectIndex(idx) }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primaryInk else colors.border,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        color = if (isSelected) colors.cardElevated else colors.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.primaryInk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = colors.secondaryText
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = colors.primaryInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
