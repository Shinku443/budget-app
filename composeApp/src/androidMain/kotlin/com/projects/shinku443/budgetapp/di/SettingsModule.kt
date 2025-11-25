// File: SettingsModule.kt
package com.projects.shinku443.budgetapp.di

import com.projects.shinku443.budgetapp.repository.SettingsRepository
import com.projects.shinku443.budgetapp.db.settingsDataStore
import com.projects.shinku443.budgetapp.notifications.AndroidNotificationScheduler
import com.projects.shinku443.budgetapp.notifications.NotificationScheduler
import com.projects.shinku443.budgetapp.settings.SecureKeyProvider
import com.projects.shinku443.budgetapp.settings.SettingsManager
import com.projects.shinku443.budgetapp.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    // Provide DataStore instance using the Android context
    single { androidContext().settingsDataStore }

    // Provide SettingsRepository
    single { SettingsRepository(get()) }

    //Provide Scheduler
    factory<NotificationScheduler> { AndroidNotificationScheduler(androidContext()) }

    // Provide SettingsViewModel
    viewModel { SettingsViewModel(get(), get()) }
}
