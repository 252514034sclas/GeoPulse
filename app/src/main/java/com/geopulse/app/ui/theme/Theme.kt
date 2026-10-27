package com.geopulse.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemePreferences {
    var isDarkTheme by mutableStateOf(true)
}

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F5D6),
    onPrimaryContainer = Color(0xFF0D3814),
    secondary = EvergreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBCEBE0),
    onSecondaryContainer = Color(0xFF063328),
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextDarkMuted,
    outline = CardBorderLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = LimeAccent,
    onPrimary = PineDark,
    primaryContainer = EvergreenDark,
    onPrimaryContainer = MistGreen,
    secondary = EmeraldPrimary,
    onSecondary = PineDark,
    secondaryContainer = ForestDark,
    onSecondaryContainer = MistGreen,
    tertiary = LeafGreen,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = CardBorderDark,
    error = RedAccent,
    onError = Color.Black
)

@Composable
fun GeoPulseTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (ThemePreferences.isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
