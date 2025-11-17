package com.projects.shinku443.budget_app.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// This creates a singleton DataStore<Preferences> tied to the app context
val Context.settingsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "settings")
