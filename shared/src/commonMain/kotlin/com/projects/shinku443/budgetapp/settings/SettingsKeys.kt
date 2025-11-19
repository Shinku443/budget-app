package com.projects.shinku443.budgetapp.settings


import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

object SettingsKeys {
    val THEME = stringPreferencesKey("theme")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val LANGUAGE = stringPreferencesKey("language")
}
