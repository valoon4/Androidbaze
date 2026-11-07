package com.valoon4.communicationtrainer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Hell"),
    DARK("Dunkel")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedConversationId: String? = null
)

private val Context.preferencesStore by preferencesDataStore(name = "communicationtrainer_local_settings")

class AppPreferences(context: Context) {
    private val store = context.applicationContext.preferencesStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_CONVERSATION = stringPreferencesKey("selected_conversation")
    }

    val settings: Flow<AppSettings> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs: Preferences ->
            AppSettings(
                themeMode = ThemeMode.entries.firstOrNull {
                    it.name == prefs[Keys.THEME_MODE]
                } ?: ThemeMode.SYSTEM,
                selectedConversationId = prefs[Keys.SELECTED_CONVERSATION]
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    suspend fun selectConversation(id: String?) {
        store.edit { prefs ->
            if (id == null) prefs.remove(Keys.SELECTED_CONVERSATION)
            else prefs[Keys.SELECTED_CONVERSATION] = id
        }
    }
}
