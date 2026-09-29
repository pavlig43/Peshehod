package ru.pavlig43.peshehod.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PeshehodBlack = Color(0xFF0A0A0A)
val PeshehodSurface = Color(0xFF1F1F1F)
val PeshehodGold = Color(0xFFFFD700)
val PeshehodAmber = Color(0xFFFFB300)
val PeshehodText = Color(0xFFE4E2E6)
val EasyRoute = Color(0xFF43A047)
val MediumRoute = Color(0xFFFDD835)
val HardRoute = Color(0xFFE53935)

private val DarkColors = darkColorScheme(
    primary = PeshehodGold,
    secondary = PeshehodAmber,
    background = PeshehodBlack,
    surface = PeshehodSurface,
    onPrimary = PeshehodBlack,
    onSecondary = PeshehodBlack,
    onBackground = PeshehodText,
    onSurface = PeshehodText,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF705D00),
    secondary = Color(0xFF765A00),
    background = Color(0xFFFFF9EE),
    surface = Color(0xFFFFF9EE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF211B00),
    onSurface = Color(0xFF211B00),
)

@Composable
fun PeshehodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
