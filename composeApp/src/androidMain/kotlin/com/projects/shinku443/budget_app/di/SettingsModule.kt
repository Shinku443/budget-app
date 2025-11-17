// File: SettingsModule.kt
package com.projects.shinku443.budget_app.di

import com.projects.shinku443.budget_app.repository.SettingsRepository
import com.projects.shinku443.budget_app.db.settingsDataStore
import com.projects.shinku443.budget_app.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    // Provide DataStore instance using the Android context
    single { androidContext().settingsDataStore }

    // Provide SettingsRepository
    single { SettingsRepository(get()) }

    // Provide SettingsViewModel
    viewModel { SettingsViewModel(get()) }
}
