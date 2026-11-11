package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Apple iOS OLED Pure Black Tokens
val IosPureBlack = Color(0xFF000000)
val IosSurfacePrimary = Color(0xFF121214)
val IosSurfaceSecondary = Color(0xFF1C1C1E)
val IosSurfaceElevated = Color(0xFF242426)
val IosBorderSubtle = Color(0x14FFFFFF) // 1px solid rgba(255, 255, 255, 0.08)
val IosTextPrimary = Color(0xFFFFFFFF)
val IosTextSecondary = Color(0xFF8E8E93)
val IosTextMuted = Color(0xFF48484A)
val IosGreen = Color(0xFF30D158)
val IosOrange = Color(0xFFFF9F0A)
val IosRed = Color(0xFFFF453A)

// Legacy aliases kept for backwards compatibility
val ObsidianBackground = IosPureBlack
val ObsidianSurface = IosSurfacePrimary
val ObsidianCard = IosSurfaceSecondary
val ObsidianCardElevated = IosSurfaceElevated
val ObsidianBorder = IosBorderSubtle
val ElectricCyan = Color(0xFF38BDF8)
val NeonEmerald = IosGreen
val NeonEmeraldGlow = IosGreen
val CyberPurple = Color(0xFFA78BFA)
val AmberWarning = IosOrange
val CoralAlert = IosRed
val TextPrimary = IosTextPrimary
val TextSecondary = IosTextSecondary
val TextMuted = IosTextMuted

@Immutable
data class AstralColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardElevated: Color,
    val border: Color,
    val primaryInk: Color,       // Crisp soft white on dark, deep charcoal on light
    val onPrimaryInk: Color,     // Deep charcoal on dark, pure white on light
    val secondaryText: Color,
    val mutedText: Color,
    val watermark: Color,
    val pillBackground: Color,
    val activeGreen: Color,
    val warningOrange: Color
)

// Apple iOS / SwiftUI OLED Pure Black Dark Palette
val DarkAstralColors = AstralColors(
    isDark = true,
    background = Color(0xFF000000),      // Pure OLED Black
    surface = Color(0xFF121214),         // Container background
    card = Color(0xFF1C1C1E),            // Apple secondary system background
    cardElevated = Color(0xFF242426),    // Elevated card
    border = Color(0x14FFFFFF),          // rgba(255, 255, 255, 0.08)
    primaryInk = Color(0xFFFFFFFF),
    onPrimaryInk = Color(0xFF000000),
    secondaryText = Color(0xFF8E8E93),   // Apple secondary label
    mutedText = Color(0xFF48484A),       // Apple placeholder/muted
    watermark = Color(0xFF0D0D0F),
    pillBackground = Color(0xFF1A1A1E),
    activeGreen = Color(0xFF30D158),     // Apple system green
    warningOrange = Color(0xFFFF9F0A)    // Apple system orange
)

// Apple iOS Light Palette
val LightAstralColors = AstralColors(
    isDark = false,
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFE5E5EA),
    border = Color(0x1A000000),
    primaryInk = Color(0xFF000000),
    onPrimaryInk = Color(0xFFFFFFFF),
    secondaryText = Color(0xFF8E8E93),
    mutedText = Color(0xFFC7C7CC),
    watermark = Color(0xFFE5E5EA),
    pillBackground = Color(0xFFE5E5EA),
    activeGreen = Color(0xFF34C759),
    warningOrange = Color(0xFFFF9500)
)

val LocalAstralColors = staticCompositionLocalOf { DarkAstralColors }
