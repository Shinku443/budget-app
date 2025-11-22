package com.projects.shinku443.budgetapp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.projects.shinku443.budgetapp.settings.Settings.Theme
import com.projects.shinku443.budgetapp.settings.SettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {


    val loggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SettingsKeys.LOGGED_IN] ?: false
    }

    val theme: Flow<Theme> = dataStore.data.map { prefs ->
        val value = prefs[SettingsKeys.THEME] ?: Theme.LIGHT.name
        Theme.valueOf(value)
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SettingsKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[SettingsKeys.LANGUAGE] ?: "en"
    }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs -> prefs[SettingsKeys.THEME] = theme.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SettingsKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs -> prefs[SettingsKeys.LANGUAGE] = language }
    }


    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { prefs -> prefs[SettingsKeys.LOGGED_IN] = value }
    }
}
