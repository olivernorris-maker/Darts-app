package com.pdcdarts.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D38),
    onPrimaryContainer = AccentGreen,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    tertiary = AccentBlue,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = Color(0xFFFF6B6B),
    onError = Color.Black,
)

@Composable
fun PDCDartsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
