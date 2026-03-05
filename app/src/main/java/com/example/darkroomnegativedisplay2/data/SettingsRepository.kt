package com.example.darkroomnegativedisplay2.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Repository for managing app settings using DataStore
 */
class SettingsRepository(
    private val context: Context
) {

    private object PreferencesKeys {
        val PRE_DISPLAY_BLACK_SECONDS = intPreferencesKey("pre_display_black_seconds")
        val DISPLAY_DURATION_SECONDS = intPreferencesKey("display_duration_seconds")
        val POST_DISPLAY_BLACK_SECONDS = intPreferencesKey("post_display_black_seconds")
        val TEST_IRRADIATION_PARTS = intPreferencesKey("test_irradiation_parts")
        val IS_INTERFACE_RED = booleanPreferencesKey("is_interface_red")
        val USE_DEVICE_BRIGHTNESS = booleanPreferencesKey("use_device_brightness")
    }

    val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            preDisplayBlackSeconds = preferences[PreferencesKeys.PRE_DISPLAY_BLACK_SECONDS] ?: 2,
            displayDurationSeconds = preferences[PreferencesKeys.DISPLAY_DURATION_SECONDS] ?: 10,
            postDisplayBlackSeconds = preferences[PreferencesKeys.POST_DISPLAY_BLACK_SECONDS] ?: 2,
            testIrradiationParts = preferences[PreferencesKeys.TEST_IRRADIATION_PARTS] ?: 10,
            isInterfaceRed = preferences[PreferencesKeys.IS_INTERFACE_RED] ?: false,
            useDeviceBrightness = preferences[PreferencesKeys.USE_DEVICE_BRIGHTNESS] ?: true
        )
    }

    suspend fun updatePreDisplayBlackSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRE_DISPLAY_BLACK_SECONDS] = seconds
        }
    }

    suspend fun updateDisplayDurationSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_DURATION_SECONDS] = seconds
        }
    }

    suspend fun updatePostDisplayBlackSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POST_DISPLAY_BLACK_SECONDS] = seconds
        }
    }

    suspend fun updateTestIrradiationParts(parts: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEST_IRRADIATION_PARTS] = parts
        }
    }

    suspend fun updateInterfaceRed(isRed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_INTERFACE_RED] = isRed
        }
    }

    suspend fun updateDeviceBrightness(useDeviceBrightness: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DEVICE_BRIGHTNESS] = useDeviceBrightness
        }
    }
}
