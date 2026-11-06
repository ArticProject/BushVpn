package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnTunnelSettings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

private data class CountryPreset(
    val code: String,
    val nameRu: String,
    val cityRu: String,
    val flag: String,
    val mapX: Float,
    val mapY: Float
)

private val countryPresets = listOf(
    CountryPreset("FI", "Финляндия", "Хельсинки", "🇫🇮", 0.552f, 0.225f),
    CountryPreset("DE", "Германия", "Франкфурт", "🇩🇪", 0.512f, 0.310f),
    CountryPreset("PL", "Польша", "Варшава", "🇵🇱", 0.538f, 0.305f),
    CountryPreset("NL", "Нидерланды", "Амстердам", "🇳🇱", 0.495f, 0.295f),
    CountryPreset("CH", "Швейцария", "Цюрих", "🇨🇭", 0.508f, 0.330f),
    CountryPreset("US", "США", "Нью-Йорк", "🇺🇸", 0.275f, 0.365f),
    CountryPreset("GB", "Великобритания", "Лондон", "🇬🇧", 0.475f, 0.300f),
    CountryPreset("TR", "Турция", "Стамбул", "🇹🇷", 0.568f, 0.375f),
    CountryPreset("KZ", "Казахстан", "Алматы", "🇰🇿", 0.675f, 0.355f),
    CountryPreset("JP", "Япония", "Токио", "🇯🇵", 0.855f, 0.390f)
)

@Composable
fun AddOrEditVlessDialog(
    initialServer: VlessServerEntity? = null,
    onDismiss: () -> Unit,
    onImportUri: (String) -> Unit,
    onSaveCustomServer: (VlessServerEntity, Boolean) -> Unit
) {
    val colors = LocalAstralColors.current
    val isEdit = initialServer != null
    var activeTab by remember { mutableIntStateOf(if (isEdit) 1 else 0) }
    val clipboardManager = LocalClipboardManager.current

    var uriInput by remember { mutableStateOf("") }
    val parsedPreview = remember(uriInput) {
        if (uriInput.trim().startsWith("vless://", ignoreCase = true)) {
            VlessParser.parseUri(uriInput).getOrNull()
        } else null
    }

    var selectedCountry by remember {
        mutableStateOf(
            countryPresets.find { it.code == initialServer?.countryCode } ?: countryPresets.first()
        )
    }
    var name by remember { mutableStateOf(initialServer?.name ?: "Хельсинки Reality #2") }
    var host by remember { mutableStateOf(initialServer?.host ?: "fi-hel-02.bushvpn.net") }
    var portText by remember { mutableStateOf((initialServer?.port ?: 443).toString()) }
    var uuid by remember { mutableStateOf(initialServer?.uuid ?: VlessParser.generateRandomUuid()) }
    var security by remember { mutableStateOf(initialServer?.security ?: "reality") }
    var networkType by remember { mutableStateOf(initialServer?.networkType ?: "tcp") }
    var flow by remember { mutableStateOf(initialServer?.flow ?: "xtls-rprx-vision") }
    var sni by remember { mutableStateOf(initialServer?.sni ?: "www.apple.com") }
    var fingerprint by remember { mutableStateOf(initialServer?.fingerprint ?: "chrome") }
    var publicKey by remember {
        mutableStateOf(
            initialServer?.publicKey ?: "kP9mW2xL7vQ4nR8jF1dC5bV0zX3yA6wE9rT2uI4oP1M"
        )
    }
    var shortId by remember { mutableStateOf(initialServer?.shortId ?: "6ba85179e30d4fc2") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 660.dp)
                .border(1.dp, colors.border, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Sovereign Node" else "Add VLESS Enclave",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.primaryInk
                        )
                        Text(
                            text = "VLESS REALITY • XTLS-Vision • WS / gRPC",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = colors.secondaryText
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_server_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isEdit) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(colors.cardElevated)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "vless:// URI" to Icons.Default.Link,
                            "Manual Setup" to Icons.Default.Tune
                        )
                        tabs.forEachIndexed { index, (title, icon) ->
                            val selected = activeTab == index
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) colors.primaryInk else Color.Transparent)
                                    .clickable { activeTab = index }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .testTag("server_dialog_tab_$index"),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (selected) colors.onPrimaryInk else colors.secondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selected) colors.onPrimaryInk else colors.secondaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (activeTab == 0 && !isEdit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Paste your vless://uuid@host:port?... access key",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 13.sp,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uriInput,
                            onValueChange = { uriInput = it },
                            placeholder = {
                                Text(
                                    "vless://7f2c1b09-44a8-4d91-b362-8e4a19f05c77@fi-hel-01.bushvpn.net:443?encryption=none&security=reality&sni=www.apple.com&fp=chrome&pbk=...&type=tcp&flow=xtls-rprx-vision#HEL-Finland",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.mutedText
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp)
                                .testTag("vless_uri_input"),
                            textStyle = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = colors.primaryInk
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryInk,
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        uriInput = clip
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("paste_clipboard_button"),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    modifier = Modifier.size(15.dp),
                                    tint = colors.primaryInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste", color = colors.primaryInk)
                            }

                            OutlinedButton(
                                onClick = {
                                    uriInput =
                                        "vless://f8c41290-3b19-4e7a-9c44-8a1e72b90d12@se-sto-01.bushvpn.net:443?encryption=none&security=reality&sni=www.apple.com&fp=chrome&pbk=mH8X9vR2kP7qW4nL6jF1dC5bV0zX3yA8wE9rT2uI4oP&sid=4a8b12c9&type=tcp&flow=xtls-rprx-vision#Стокгольм"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fill_demo_vless_button"),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Sample Key",
                                    modifier = Modifier.size(15.dp),
                                    tint = colors.primaryInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Demo VLESS", color = colors.primaryInk)
                            }
                        }

                        if (parsedPreview != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.cardElevated)
                                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${parsedPreview.nodeAirportCode} • ${parsedPreview.name}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.primaryInk
                                )
                                Text(
                                    text = "${parsedPreview.host}:${parsedPreview.port} • ${parsedPreview.protocolBadge}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.secondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onImportUri(uriInput) },
                            enabled = uriInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_import_vless_button"),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryInk,
                                contentColor = colors.onPrimaryInk
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Import VLESS Enclave",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            countryPresets.forEach { preset ->
                                val isSelected = preset.code == selectedCountry.code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCountry = preset
                                        name = preset.cityRu
                                    },
                                    label = { Text("${preset.code} • ${preset.cityRu}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryInk,
                                        selectedLabelColor = colors.onPrimaryInk
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("City / Node Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = host,
                                onValueChange = { host = it },
                                label = { Text("Host / IP") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = portText,
                                onValueChange = { portText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Port") },
                                singleLine = true,
                                modifier = Modifier.weight(0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        OutlinedTextField(
                            value = uuid,
                            onValueChange = { uuid = it },
                            label = { Text("VLESS UUID") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { uuid = VlessParser.generateRandomUuid() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "New UUID",
                                        tint = colors.primaryInk
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("reality", "tls", "none").forEach { sec ->
                                FilterChip(
                                    selected = security == sec,
                                    onClick = { security = sec },
                                    label = { Text(sec.uppercase()) }
                                )
                            }
                            listOf("tcp", "ws", "grpc").forEach { net ->
                                FilterChip(
                                    selected = networkType == net,
                                    onClick = { networkType = net },
                                    label = { Text(net.uppercase()) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sni,
                                onValueChange = { sni = it },
                                label = { Text("SNI") },
                                singleLine = true,
                                modifier = Modifier.weight(0.6f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = fingerprint,
                                onValueChange = { fingerprint = it },
                                label = { Text("uTLS FP") },
                                singleLine = true,
                                modifier = Modifier.weight(0.4f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val entity = VlessServerEntity(
                                    id = initialServer?.id ?: 0L,
                                    name = name.ifBlank { selectedCountry.cityRu },
                                    countryCode = selectedCountry.code,
                                    countryName = selectedCountry.nameRu,
                                    city = selectedCountry.cityRu,
                                    flagEmoji = selectedCountry.flag,
                                    host = host.ifBlank { "fi-hel-01.bushvpn.net" },
                                    port = portText.toIntOrNull() ?: 443,
                                    uuid = uuid.ifBlank { VlessParser.generateRandomUuid() },
                                    security = security,
                                    flow = flow,
                                    networkType = networkType,
                                    sni = sni,
                                    fingerprint = fingerprint,
                                    publicKey = publicKey,
                                    shortId = shortId,
                                    pingMs = initialServer?.pingMs ?: 31,
                                    loadPercent = initialServer?.loadPercent ?: 18,
                                    bandwidthGbps = 10,
                                    isFavorite = true,
                                    isCustom = true,
                                    category = "P2P",
                                    mapX = selectedCountry.mapX,
                                    mapY = selectedCountry.mapY
                                )
                                onSaveCustomServer(entity, isEdit)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryInk,
                                contentColor = colors.onPrimaryInk
                            )
                        ) {
                            Text(
                                text = "Save VLESS Node",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XrayConfigInspectorDialog(
    server: VlessServerEntity,
    settings: VpnTunnelSettings,
    onDismiss: () -> Unit,
    onCopied: (String) -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    val vlessUri = remember(server) { VlessParser.toUri(server) }
    val xrayJson = remember(server, settings) { VlessParser.toXrayJsonConfig(server, settings) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .border(1.dp, colors.border, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Inspect (${server.nodeAirportCode}) • ${server.city}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.primaryInk
                        )
                        Text(
                            text = "${server.protocolBadge} • SNI: ${server.sni} • ${server.pingMs}ms",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = colors.secondaryText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VLESS SHARE URI",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = vlessUri,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = colors.primaryInk,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(vlessUri))
                            onCopied("Copied vless:// URI")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryInk,
                            contentColor = colors.onPrimaryInk
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy vless://", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(xrayJson))
                            onCopied("Copied X-Ray JSON")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.primaryInk
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy JSON", fontSize = 12.sp, color = colors.primaryInk)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "X-RAY CORE CONFIG (JSON)",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = xrayJson,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}
