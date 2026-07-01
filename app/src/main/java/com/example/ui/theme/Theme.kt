package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremierLeagueColorScheme = darkColorScheme(
    primary = PlSleekPurple,
    secondary = PlSkyBlue,
    tertiary = PlGold,
    background = PlDarkPurple,
    surface = PlCardPurple,
    error = PlErrorRed,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F0F0F),
    onTertiary = Color(0xFF0F0F0F),
    onBackground = PlTextPrimary,
    onSurface = PlTextPrimary,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremierLeagueColorScheme,
        typography = Typography,
        content = content
    )
}
