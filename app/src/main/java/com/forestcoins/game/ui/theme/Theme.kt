package com.forestcoins.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    background = ForestBackground,
    surface = ForestBackground,
    onPrimary = ColorSchemeTokens.onPrimary,
    onSecondary = ColorSchemeTokens.onSecondary,
    onBackground = ColorSchemeTokens.onBackgroundLight,
    onSurface = ColorSchemeTokens.onBackgroundLight
)

private val DarkColors = darkColorScheme(
    primary = ForestPrimaryDark,
    secondary = ForestSecondary,
    background = ForestBackgroundDark,
    surface = ForestBackgroundDark,
    onPrimary = ColorSchemeTokens.onPrimaryDark,
    onSecondary = ColorSchemeTokens.onSecondaryDark,
    onBackground = ColorSchemeTokens.onBackgroundDark,
    onSurface = ColorSchemeTokens.onBackgroundDark
)

@Composable
fun ForestCoinsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private object ColorSchemeTokens {
    val onPrimary = androidx.compose.ui.graphics.Color.White
    val onSecondary = androidx.compose.ui.graphics.Color(0xFF442200)
    val onPrimaryDark = androidx.compose.ui.graphics.Color(0xFF00390C)
    val onSecondaryDark = androidx.compose.ui.graphics.Color(0xFF3B1E00)
    val onBackgroundLight = androidx.compose.ui.graphics.Color(0xFF1B2A1C)
    val onBackgroundDark = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
}
