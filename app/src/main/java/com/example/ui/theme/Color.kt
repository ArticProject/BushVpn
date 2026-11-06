package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Legacy aliases kept for compatibility if referenced
val ObsidianBackground = Color(0xFF070B12)
val ObsidianSurface = Color(0xFF0E1522)
val ObsidianCard = Color(0xFF141D2E)
val ObsidianCardElevated = Color(0xFF1B273D)
val ObsidianBorder = Color(0xFF23324D)
val ElectricCyan = Color(0xFF00F0FF)
val NeonEmerald = Color(0xFF10B981)
val NeonEmeraldGlow = Color(0xFF34D399)
val CyberPurple = Color(0xFF8B5CF6)
val AmberWarning = Color(0xFFF59E0B)
val CoralAlert = Color(0xFFEF4444)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

@Immutable
data class AstralColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardElevated: Color,
    val border: Color,
    val primaryInk: Color,       // White on dark, Black on light
    val onPrimaryInk: Color,     // Black on dark, White on light
    val secondaryText: Color,
    val mutedText: Color,
    val watermark: Color,
    val pillBackground: Color,
    val activeGreen: Color,
    val warningOrange: Color
)

val DarkAstralColors = AstralColors(
    isDark = true,
    background = Color(0xFF000000),
    surface = Color(0xFF0B0B0C),
    card = Color(0xFF121214),
    cardElevated = Color(0xFF1C1C1F),
    border = Color(0xFF252529),
    primaryInk = Color(0xFFFFFFFF),
    onPrimaryInk = Color(0xFF000000),
    secondaryText = Color(0xFF98989F),
    mutedText = Color(0xFF5E5E66),
    watermark = Color(0xFF151517),
    pillBackground = Color(0xFF18181B),
    activeGreen = Color(0xFF10B981),
    warningOrange = Color(0xFFF59E0B)
)

val LightAstralColors = AstralColors(
    isDark = false,
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFF0F0F2),
    card = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFF2F2F4),
    border = Color(0xFFE4E4E8),
    primaryInk = Color(0xFF0D0D0F),
    onPrimaryInk = Color(0xFFFFFFFF),
    secondaryText = Color(0xFF686870),
    mutedText = Color(0xFF9A9AA2),
    watermark = Color(0xFFE8E8EC),
    pillBackground = Color(0xFFEAEAEF),
    activeGreen = Color(0xFF059669),
    warningOrange = Color(0xFFD97706)
)

val LocalAstralColors = staticCompositionLocalOf { LightAstralColors }
