package com.valoon4.emotionskompass.core.journal

import android.content.Context

/**
 * Stores ONLY whether the user enabled the optional device-credential gate.
 * No passphrases, notes or journal contents are stored in preferences.
 */
class AppLockPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("app_access_settings", Context.MODE_PRIVATE)

    val enabled: Boolean get() = preferences.getBoolean("device_credential_gate", false)

    fun setEnabled(value: Boolean) {
        check(preferences.edit().putBoolean("device_credential_gate", value).commit()) {
            "App-Sperre konnte nicht gespeichert werden."
        }
    }
}
