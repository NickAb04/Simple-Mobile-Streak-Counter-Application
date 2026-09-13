package com.riystreak.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.riystreak.app.data.model.AppSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_REMINDER_TIME = stringPreferencesKey("default_reminder_time")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val notifEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        val timeString = preferences[PreferencesKeys.DEFAULT_REMINDER_TIME]
        val reminderTime = timeString?.let {
            runCatching { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }.getOrNull()
        } ?: LocalTime.of(20, 0)
        val onboardingDone = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
        val lockEnabled = preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false

        AppSettings(
            notificationsEnabled = notifEnabled,
            defaultReminderTime = reminderTime,
            hasCompletedOnboarding = onboardingDone,
            appLockEnabled = lockEnabled
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDefaultReminderTime(time: LocalTime) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] = time.format(DateTimeFormatter.ISO_LOCAL_TIME)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }
}
