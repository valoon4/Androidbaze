package com.valoon4.emotionskompass.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingStore by preferencesDataStore(name = "onboarding")

class OnboardingPreferences(private val context: Context) {
    private val completedKey = booleanPreferencesKey("completed")
    val completed: Flow<Boolean> = context.onboardingStore.data.map { it[completedKey] ?: false }

    suspend fun complete() {
        context.onboardingStore.edit { it[completedKey] = true }
    }
}
