package com.valoon4.emotionskompass.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepPetrol = Color(0xFF34535D)
val SoftSage = Color(0xFF8AA697)
val WarmBackground = Color(0xFFF7F7F3)
val WarmSand = Color(0xFFEAE2D7)
val Ink = Color(0xFF24312F)
val MutedInk = Color(0xFF53625E)
val SoftGreen = Color(0xFFEEF2EE)

private val LightScheme = lightColorScheme(
    primary = DeepPetrol,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE9E5),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF526C60),
    onSecondary = Color.White,
    secondaryContainer = SoftGreen,
    onSecondaryContainer = Ink,
    background = WarmBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SoftGreen,
    onSurfaceVariant = MutedInk,
    outline = Color(0xFFD0DCD5)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFAECBC5),
    onPrimary = Color(0xFF183138),
    primaryContainer = Color(0xFF294A53),
    onPrimaryContainer = Color(0xFFD6EEEA),
    secondary = Color(0xFFB3D0BE),
    onSecondary = Color(0xFF1A3425),
    secondaryContainer = Color(0xFF355044),
    onSecondaryContainer = Color(0xFFD6EBD9),
    background = Color(0xFF151E1C),
    onBackground = Color(0xFFE2EBE6),
    surface = Color(0xFF202C28),
    onSurface = Color(0xFFE2EBE6),
    surfaceVariant = Color(0xFF32423B),
    onSurfaceVariant = Color(0xFFC5D5CD),
    outline = Color(0xFF7D9990)
)

@Composable
fun EmotionskompassTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = Typography(),
        content = content
    )
}
