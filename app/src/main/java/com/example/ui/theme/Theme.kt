package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Pixel16DarkColorScheme = darkColorScheme(
    primary = PixelPrimaryLilac,
    onPrimary = PixelOnPrimaryLilac,
    primaryContainer = PixelPrimaryContainer,
    onPrimaryContainer = PixelOnPrimaryContainer,
    secondary = PixelSecondary,
    onSecondary = PixelOnSecondary,
    secondaryContainer = PixelSecondaryContainer,
    onSecondaryContainer = PixelOnSecondaryContainer,
    tertiary = PixelTertiary,
    onTertiary = PixelOnTertiary,
    background = PixelDarkBackground,
    onBackground = PixelTextPrimary,
    surface = PixelDarkSurface,
    onSurface = PixelTextPrimary,
    surfaceVariant = PixelDarkSurfaceContainer,
    onSurfaceVariant = PixelTextSecondary,
    outline = PixelDarkOutline,
    outlineVariant = PixelDarkOutlineVariant,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Pixel16DarkColorScheme,
        typography = Typography,
        content = content
    )
}

