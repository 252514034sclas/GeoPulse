package com.geopulse.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

object ThemePreferences {
    private const val PREFS_NAME = "geopulse_theme_prefs"
    private const val KEY_MODE = "theme_mode"

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    val isDarkTheme: Boolean
        @Composable
        get() = when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)
        themeMode = try {
            ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun updateThemeMode(context: Context, mode: ThemeMode) {
        themeMode = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }
}

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = EvergreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF7F0),
    onSecondaryContainer = Color(0xFF1B3A22),
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderLight,
    outlineVariant = Color(0xFFD6DDD2),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = EvergreenDark,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = ForestDark,
    onSecondaryContainer = Color(0xFFA5D6A7),
    tertiary = LimeAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = CardBorderDark,
    outlineVariant = Color(0xFF354835),
    error = RedAccent,
    onError = Color.White
)

@Composable
fun GeoPulseTheme(
    darkTheme: Boolean = ThemePreferences.isDarkTheme,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        ThemePreferences.init(context)
    }

    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
