package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taskflow_prefs")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val IS_SEEDED = booleanPreferencesKey("is_seeded_v1")
        val FOCUS_SESSIONS_TODAY = intPreferencesKey("focus_sessions_today")
        val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAS_REQUESTED_NOTIFICATIONS = booleanPreferencesKey("has_requested_notifications")
        val NOTIFIED_REMINDER_KEYS = stringSetPreferencesKey("notified_reminder_keys")
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    val hasRequestedNotifications: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_REQUESTED_NOTIFICATIONS] ?: false
    }

    val focusSessionsToday: Flow<Int> = context.dataStore.data.map { prefs ->
        val savedDate = prefs[Keys.LAST_SESSION_DATE]
        if (savedDate == todayKey()) prefs[Keys.FOCUS_SESSIONS_TODAY] ?: 0 else 0
    }

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
        val today = todayKey()
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
}
