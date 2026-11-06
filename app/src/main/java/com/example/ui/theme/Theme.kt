package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val AstralDarkScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFF10B981),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121214),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1F),
    onSurfaceVariant = Color(0xFF98989F),
    outline = Color(0xFF252529)
)

private val AstralLightScheme = lightColorScheme(
    primary = Color(0xFF0D0D0F),
    onPrimary = Color.White,
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF0D0D0F),
    surface = Color.White,
    onSurface = Color(0xFF0D0D0F),
    surfaceVariant = Color(0xFFF2F2F4),
    onSurfaceVariant = Color(0xFF686870),
    outline = Color(0xFFE4E4E8)
)

@Composable
fun AetherVpnTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val astralColors = if (isDarkTheme) DarkAstralColors else LightAstralColors
    val colorScheme = if (isDarkTheme) AstralDarkScheme else AstralLightScheme

    CompositionLocalProvider(LocalAstralColors provides astralColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
