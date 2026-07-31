package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CasinoColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = PurpleDarkBackground,
    primaryContainer = PurpleSurfaceVariant,
    onPrimaryContainer = GoldPrimary,
    secondary = CyanSecondary,
    onSecondary = PurpleDarkBackground,
    background = PurpleDarkBackground,
    onBackground = TextPrimary,
    surface = PurpleSurface,
    onSurface = TextPrimary,
    surfaceVariant = PurpleSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = LoserRed,
    onError = TextPrimary
)

@Composable
fun LuckySpinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CasinoColorScheme,
        typography = Typography,
        content = content
    )
}

