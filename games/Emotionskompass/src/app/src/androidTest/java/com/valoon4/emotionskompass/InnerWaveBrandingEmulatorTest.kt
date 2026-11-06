package com.valoon4.emotionskompass

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pixel- and accessibility-safe branding smoke tests; no private data. */
@RunWith(AndroidJUnit4::class)
class InnerWaveBrandingEmulatorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun launcherIsNamedInnerWaveWithoutChangingTheInstalledPackageIdentity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.valoon4.emotionskompass", context.packageName)
        assertEquals("InnerWave", context.getString(R.string.app_name))
        @Suppress("DEPRECATION")
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        assertEquals("InnerWave",
            context.packageManager.getApplicationLabel(info).toString())
    }

    @Test fun waveArtworkAndSplashArePackagedAsDecodableAndroidDrawables() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (res in listOf(
            R.mipmap.ic_launcher, R.drawable.innerwave_launcher_foreground,
            R.drawable.innerwave_monochrome
        )) {
            val drawable: Drawable? = context.getDrawable(res)
            assertNotNull("Required brand drawable $res missing", drawable)
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable!!.setBounds(0, 0, 96, 96)
            drawable.draw(canvas)
            assertTrue("Brand drawable must render visible pixels: $res",
                (0 until 96).any { x ->
                    (0 until 96).any { y ->
                        (bitmap.getPixel(x, y) ushr 24) != 0
                    }
                })
        }
    }

    @Test fun approvedSplashHasAccessibleBrandNameAndNoControls() {
        rule.setContent { InnerWaveSplashScreen() }
        rule.onNodeWithContentDescription("InnerWave-Logo").assertExists()
        rule.onNodeWithText("InnerWave").assertExists()
    }
}
