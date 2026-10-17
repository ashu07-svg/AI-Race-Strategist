package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RaceControlDarkColorScheme = darkColorScheme(
    primary = F1Red,
    onPrimary = CockpitWhite,
    primaryContainer = F1RedDark,
    onPrimaryContainer = CockpitWhite,
    secondary = TelemetryCyan,
    onSecondary = RaceBackground,
    secondaryContainer = RaceSurfaceElevated,
    onSecondaryContainer = TelemetryCyan,
    tertiary = TireMediumYellow,
    onTertiary = RaceBackground,
    background = RaceBackground,
    onBackground = CockpitWhite,
    surface = RaceSurface,
    onSurface = CockpitWhite,
    surfaceVariant = RaceSurfaceElevated,
    onSurfaceVariant = CockpitMuted,
    outline = RaceSurfaceBorder,
    error = FlagRed,
    onError = CockpitWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Motorsport telemetry is always dark theme
    dynamicColor: Boolean = false, // Enforce motorsport branding palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RaceControlDarkColorScheme,
        typography = Typography,
        content = content
    )
}
