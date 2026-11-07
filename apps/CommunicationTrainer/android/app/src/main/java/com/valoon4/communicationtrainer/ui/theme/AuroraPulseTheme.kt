package com.valoon4.communicationtrainer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Aurora Pulse – product palette selected for CommunicationTrainer.
 *
 * Brand values (#6A6BFF indigo, #F35E91 pink, #25CFC0 mint) are used
 * decoratively. Filled controls use deeper accessible variants in light mode:
 * #5D50CF + white text (5.98:1) and #CB3470 + white (4.92:1).
 * Do not use brand pink or brand indigo with white small text on top.
 */
internal object AuroraPulseAccent {
    val BrandIndigo = Color(0xFF6A6BFF)
    val BrandPink = Color(0xFFF35E91)
    val BrandMint = Color(0xFF25CFC0)
    val RewardGold = Color(0xFFFFC85A)

    val HeroStart = Color(0xFF34226F)
    val HeroMiddle = Color(0xFF5948BF)
    val HeroEnd = Color(0xFF5D50CF)
    val HeroMint = Color(0xFFB5FFF2)
}

internal val AuroraPulseLightColors = lightColorScheme(
    primary = Color(0xFF5D50CF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E3FF),
    onPrimaryContainer = Color(0xFF26287C),
    secondary = Color(0xFFCB3470),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF6A1236),
    tertiary = Color(0xFF0F756B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE4FBF6),
    onTertiaryContainer = Color(0xFF0F5E54),
    background = Color(0xFFFCFAFF),
    onBackground = Color(0xFF27253A),
    surface = Color.White,
    onSurface = Color(0xFF27253A),
    surfaceVariant = Color(0xFFF2F0F8),
    onSurfaceVariant = Color(0xFF615E73),
    outline = Color(0xFFD6D2E3),
    error = Color(0xFFB3261E),
    onError = Color.White
)

internal val AuroraPulseDarkColors = darkColorScheme(
    primary = Color(0xFFA7A8FF),
    onPrimary = Color(0xFF2E2F7C),
    primaryContainer = Color(0xFF39347B),
    onPrimaryContainer = Color(0xFFE4E3FF),
    secondary = Color(0xFFFF9ABA),
    onSecondary = Color(0xFF5D1732),
    secondaryContainer = Color(0xFF5D1732),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = Color(0xFF5FE8DA),
    onTertiary = Color(0xFF0A403A),
    tertiaryContainer = Color(0xFF164C48),
    onTertiaryContainer = Color(0xFFDBFFF6),
    background = Color(0xFF171620),
    onBackground = Color(0xFFF6F3FF),
    surface = Color(0xFF211F2B),
    onSurface = Color(0xFFF6F3FF),
    surfaceVariant = Color(0xFF2B2836),
    onSurfaceVariant = Color(0xFFC8C2D5),
    outline = Color(0xFF706979),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun AuroraPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AuroraPulseDarkColors else AuroraPulseLightColors,
        content = content
    )
}
