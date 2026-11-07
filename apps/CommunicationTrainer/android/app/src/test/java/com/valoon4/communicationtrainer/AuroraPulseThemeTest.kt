package com.valoon4.communicationtrainer

import androidx.compose.ui.graphics.Color
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseAccent
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseDarkColors
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AuroraPulseThemeTest {
    @Test
    fun selectedBrandColorsAreRetained() {
        assertEquals(Color(0xFF6A6BFF), AuroraPulseAccent.BrandIndigo)
        assertEquals(Color(0xFFF35E91), AuroraPulseAccent.BrandPink)
        assertEquals(Color(0xFF25CFC0), AuroraPulseAccent.BrandMint)
        assertEquals(Color(0xFFFFC85A), AuroraPulseAccent.RewardGold)
    }

    @Test
    fun allImportantTextPairsMeetWcagAaInLightMode() {
        val c = AuroraPulseLightColors
        assertContrast("Primary button", c.primary, c.onPrimary)
        assertContrast("Secondary button", c.secondary, c.onSecondary)
        assertContrast("Mint button", c.tertiary, c.onTertiary)
        assertContrast("Body on background", c.background, c.onBackground)
        assertContrast("Cards", c.surface, c.onSurface)
        assertContrast("Secondary card text", c.surface, c.onSurfaceVariant)
        assertContrast("Primary container", c.primaryContainer, c.onPrimaryContainer)
        assertContrast("Pink container", c.secondaryContainer, c.onSecondaryContainer)
        assertContrast("Mint container", c.tertiaryContainer, c.onTertiaryContainer)
    }

    @Test
    fun allImportantTextPairsMeetWcagAaInDarkMode() {
        val c = AuroraPulseDarkColors
        assertContrast("Primary button", c.primary, c.onPrimary)
        assertContrast("Secondary button", c.secondary, c.onSecondary)
        assertContrast("Mint button", c.tertiary, c.onTertiary)
        assertContrast("Body on background", c.background, c.onBackground)
        assertContrast("Cards", c.surface, c.onSurface)
        assertContrast("Secondary card text", c.surface, c.onSurfaceVariant)
        assertContrast("Primary container", c.primaryContainer, c.onPrimaryContainer)
        assertContrast("Pink container", c.secondaryContainer, c.onSecondaryContainer)
        assertContrast("Mint container", c.tertiaryContainer, c.onTertiaryContainer)
    }

    @Test
    fun decorativeHeroSupportsWhiteText() {
        for (color in listOf(AuroraPulseAccent.HeroStart, AuroraPulseAccent.HeroMiddle, AuroraPulseAccent.HeroEnd)) {
            assertContrast("Hero", color, Color.White)
        }
    }

    private fun assertContrast(label: String, background: Color, foreground: Color) {
        assertTrue(
            "$label contrast must reach 4.5:1 but was ${contrastRatio(background, foreground)}",
            contrastRatio(background, foreground) >= 4.5
        )
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val l1 = relativeLuminance(a)
        val l2 = relativeLuminance(b)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val value = channel.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) +
            0.7152 * linear(color.green) +
            0.0722 * linear(color.blue)
    }
}
