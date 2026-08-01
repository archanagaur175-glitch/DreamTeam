package com.dreamteam.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DreamColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = TextPrimary,
    secondary = AccentIndigo,
    onSecondary = TextPrimary,
    tertiary = AccentAmber,
    onTertiary = NightBase,
    background = NightBase,
    onBackground = TextPrimary,
    surface = NightSurface,
    onSurface = TextPrimary,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = NightBorder,
    outlineVariant = NightBorder,
    error = DangerRed,
    onError = NightBase,
)

/**
 * Dark-first, deliberately non-dynamic theme. The app is a night/energy instrument
 * panel, so light mode and dynamic color are intentionally not offered in v1.
 */
@Composable
fun DreamTeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DreamColorScheme,
        typography = DreamTypography,
        content = content,
    )
}
