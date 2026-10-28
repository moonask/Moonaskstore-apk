package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MoonAskCyanAccent,
    onPrimary = MoonAskNavy,
    primaryContainer = MoonAskBlueDark,
    onPrimaryContainer = MoonAskCyanLight,
    secondary = MoonAskCyanLight,
    onSecondary = MoonAskNavy,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = MoonAskAmber,
    onTertiary = Color.Black,
    background = MoonAskNavy,
    onBackground = MoonAskTextPrimaryDark,
    surface = MoonAskSurfaceDark,
    onSurface = MoonAskTextPrimaryDark,
    surfaceVariant = MoonAskCardDark,
    onSurfaceVariant = MoonAskTextSecondaryDark,
    outline = MoonAskBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = MoonAskBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = MoonAskBlueDark,
    secondary = MoonAskCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = MoonAskAmber,
    onTertiary = Color.White,
    background = MoonAskBackgroundLight,
    onBackground = MoonAskTextPrimaryLight,
    surface = MoonAskSurfaceLight,
    onSurface = MoonAskTextPrimaryLight,
    surfaceVariant = MoonAskCardLight,
    onSurfaceVariant = MoonAskTextSecondaryLight,
    outline = MoonAskBorderLight
)

@Composable
fun MoonAskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep MoonAsk signature brand palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MoonAskTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
