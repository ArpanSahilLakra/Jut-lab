package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    companion object {
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
    }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_EFFECTS_ENABLED] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[HAPTICS_ENABLED] ?: true }
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS_ENABLED] ?: true }
    val reduceMotion: Flow<Boolean> = context.dataStore.data.map { it[REDUCE_MOTION] ?: false }
    val soundVolume: Flow<Float> = context.dataStore.data.map { it[SOUND_VOLUME] ?: 1.0f }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_EFFECTS_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { it[REDUCE_MOTION] = enabled }
    }

    suspend fun setSoundVolume(volume: Float) {
        context.dataStore.edit { it[SOUND_VOLUME] = volume }
    }
}
