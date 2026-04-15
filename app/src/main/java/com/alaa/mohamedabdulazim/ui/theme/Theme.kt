package com.alaa.mohamedabdulazim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = IslamicGreen,
    onPrimary          = IslamicCream,
    primaryContainer   = CardBackground,
    onPrimaryContainer = IslamicGreenDark,
    secondary          = IslamicGold,
    onSecondary        = IslamicGreenDark,
    secondaryContainer = IslamicBeige,
    background         = IslamicCream,
    surface            = IslamicCream,
    onBackground       = IslamicGreenDark,
    onSurface          = IslamicGreenDark
)

private val DarkColors = darkColorScheme(
    primary            = IslamicGreenLight,
    onPrimary          = IslamicGreenDark,
    primaryContainer   = IslamicGreenDark,
    secondary          = IslamicGold,
    onSecondary        = IslamicGreenDark,
    background         = Color(0xFF0A1A0A),
    surface            = Color(0xFF1A2E1A),
    onBackground       = IslamicCream,
    onSurface          = IslamicCream
)

@Composable
fun IslamicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content
    )
}
