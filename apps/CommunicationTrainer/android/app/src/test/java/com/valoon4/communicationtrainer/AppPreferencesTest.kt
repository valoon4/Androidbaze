package com.valoon4.communicationtrainer

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.valoon4.communicationtrainer.data.AppPreferences
import com.valoon4.communicationtrainer.data.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppPreferencesTest {
    @Test
    fun themeAndSelectedConversationSurviveNewRepositoryInstance() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstStore = AppPreferences(context)
        firstStore.setThemeMode(ThemeMode.DARK)
        firstStore.selectConversation("example-id")
        val secondStore = AppPreferences(context)
        assertEquals(ThemeMode.DARK, secondStore.settings.first().themeMode)
        assertEquals("example-id", secondStore.settings.first().selectedConversationId)
        secondStore.selectConversation(null)
        assertNull(firstStore.settings.first().selectedConversationId)
        secondStore.setThemeMode(ThemeMode.SYSTEM)
    }
}
