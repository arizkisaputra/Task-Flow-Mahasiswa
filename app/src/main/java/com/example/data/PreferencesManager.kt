package com.example.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.util.DateTimeFormatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taskflow_prefs")

@Immutable
data class PrefsSnapshot(
    val focusSessionsToday: Int = 0,
    val soundEnabled: Boolean = true,
    val hasRequestedNotifications: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val IS_SEEDED = booleanPreferencesKey("is_seeded_v1")
        val FOCUS_SESSIONS_TODAY = intPreferencesKey("focus_sessions_today")
        val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAS_REQUESTED_NOTIFICATIONS = booleanPreferencesKey("has_requested_notifications")
        val NOTIFIED_REMINDER_KEYS = stringSetPreferencesKey("notified_reminder_keys")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val prefsSnapshot: Flow<PrefsSnapshot> = context.dataStore.data
        .map { prefs ->
            val savedDate = prefs[Keys.LAST_SESSION_DATE]
            val sessionsToday = if (savedDate == DateTimeFormatters.todayStorageKey()) {
                prefs[Keys.FOCUS_SESSIONS_TODAY] ?: 0
            } else {
                0
            }
            PrefsSnapshot(
                focusSessionsToday = sessionsToday,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                hasRequestedNotifications = prefs[Keys.HAS_REQUESTED_NOTIFICATIONS] ?: false,
                themeMode = ThemeMode.fromStorage(prefs[Keys.THEME_MODE])
            )
        }
        .distinctUntilChanged()

    suspend fun isSeeded(): Boolean {
        return context.dataStore.data.first()[Keys.IS_SEEDED] ?: false
    }

    suspend fun markSeeded() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SEEDED] = true
        }
    }

    suspend fun markNotificationsRequested() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_REQUESTED_NOTIFICATIONS] = true
        }
    }

    suspend fun incrementFocusSessionToday() {
        val today = DateTimeFormatters.todayStorageKey()
        context.dataStore.edit { prefs ->
            val current = if (prefs[Keys.LAST_SESSION_DATE] == today) {
                prefs[Keys.FOCUS_SESSIONS_TODAY] ?: 0
            } else {
                0
            }
            prefs[Keys.LAST_SESSION_DATE] = today
            prefs[Keys.FOCUS_SESSIONS_TODAY] = current + 1
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun getNotifiedReminderKeys(): Set<String> {
        return context.dataStore.data.first()[Keys.NOTIFIED_REMINDER_KEYS] ?: emptySet()
    }

    suspend fun addNotifiedReminderKey(key: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.NOTIFIED_REMINDER_KEYS] ?: emptySet()
            val trimmed = if (current.size >= 100) current.toList().takeLast(60).toSet() else current
            prefs[Keys.NOTIFIED_REMINDER_KEYS] = trimmed + key
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }
}

