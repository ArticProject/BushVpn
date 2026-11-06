package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProfileScreen(
    operatorName: String,
    operatorUid: String,
    avatarFilePath: String?,
    isEnglish: Boolean,
    isDarkTheme: Boolean,
    onUpdateOperatorName: (String) -> Unit,
    onPickAvatarUri: (android.net.Uri) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onPreviewNoInternet: () -> Unit,
    onCopiedMessage: (String) -> Unit
) {
    val colors = LocalAstralColors.current
    val clipboardManager = LocalClipboardManager.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember(operatorName) { mutableStateOf(operatorName) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickAvatarUri(uri)
        }
    }

    val customAvatarBitmap = remember(avatarFilePath) {
        if (!avatarFilePath.isNullOrBlank() && File(avatarFilePath).exists()) {
            runCatching { BitmapFactory.decodeFile(avatarFilePath)?.asImageBitmap() }.getOrNull()
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row: PROFILE / Sovereign Operator + Quick Theme & Language Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PROFILE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.8.sp,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sovereign Operator",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp,
                        color = colors.primaryInk
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.card)
                            .border(1.dp, colors.border, RoundedCornerShape(50))
                            .clickable(onClick = onToggleLanguage)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("profile_lang_toggle")
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
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.card)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("profile_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme",
                            tint = colors.primaryInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Card 1: Sovereign Operator Avatar + Name + Edit + UID Pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(26.dp)),
                color = colors.card,
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141416))
                            .border(2.dp, colors.border, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("profile_avatar_picker"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customAvatarBitmap != null) {
                            Image(
                                bitmap = customAvatarBitmap,
                                contentDescription = "Operator Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Canvas(modifier = Modifier.size(44.dp)) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = size.minDimension * 0.34f
                                for (i in 0 until 8) {
                                    val angle = (i * (2f * PI / 8f)).toFloat()
                                    val dx = cx + cos(angle) * r
                                    val dy = cy + sin(angle) * r
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.25f),
                                        radius = 3.2.dp.toPx(),
                                        center = Offset(dx, dy)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = operatorName,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = colors.primaryInk
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(colors.cardElevated)
                                    .clickable {
                                        tempName = operatorName
                                        showEditNameDialog = true
                                    }
                                    .testTag("edit_operator_name_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.cardElevated)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(operatorUid))
                                    onCopiedMessage(
                                        if (isEnglish) "UID $operatorUid copied" else "UID $operatorUid скопирован"
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("copy_uid_pill"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UID:  $operatorUid",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = colors.secondaryText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UID",
                                tint = colors.secondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: AUTHORSHIP ("Делал: @23")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
                color = colors.card,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTHORSHIP",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.4.sp,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Делал: @23",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.primaryInk
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.cardElevated)
                            .border(1.dp, colors.border, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "@23",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.primaryInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick utility pills: Replay Intro & Preview Offline Animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onReplayOnboarding)
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .testTag("replay_onboarding_button"),
                    color = colors.card,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Intro",
                            tint = colors.primaryInk,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Replay Intro" else "Онбординг",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.primaryInk
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onPreviewNoInternet)
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .testTag("preview_no_internet_button"),
                    color = colors.card,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline animation",
                            tint = colors.primaryInk,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Offline State" else "Нет сети (Демо)",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.primaryInk
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Footer Manifesto & ASTRAL Watermark
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MINIMAL SOVEREIGN TUNNEL ROUTING",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    color = colors.mutedText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "END-TO-END CRYPTOGRAPHIC ENCLAVE",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    color = colors.mutedText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "DEVELOPED FOR PRIVATE CITIZENS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    color = colors.mutedText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ASTRAL",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 66.sp,
                letterSpacing = 3.sp,
                color = colors.watermark,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 104.dp)
            )
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = colors.card,
            titleContentColor = colors.primaryInk,
            title = {
                Text(
                    text = if (isEnglish) "Operator Callsign" else "Имя оператора",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    label = { Text(if (isEnglish) "Operator Name" else "Позывной") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOperatorName(tempName)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryInk,
                        contentColor = colors.onPrimaryInk
                    )
                ) {
                    Text(if (isEnglish) "Save" else "Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(if (isEnglish) "Cancel" else "Отмена", color = colors.secondaryText)
                }
            }
        )
    }
}
