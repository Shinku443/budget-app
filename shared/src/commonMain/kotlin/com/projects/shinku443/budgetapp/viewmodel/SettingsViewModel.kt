package com.projects.shinku443.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budgetapp.notifications.NotificationScheduler
import com.projects.shinku443.budgetapp.repository.SettingsRepository
import com.projects.shinku443.budgetapp.settings.Settings.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepo: SettingsRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    // Reactive flows backed by DataStore
    val theme: StateFlow<Theme> = settingsRepo.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme.LIGHT)

    val notificationsEnabled: StateFlow<Boolean> = settingsRepo.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val language: StateFlow<String> = settingsRepo.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    // Update methods
    fun setTheme(theme: Theme) {
        viewModelScope.launch { settingsRepo.setTheme(theme) }
    }
//
//    fun setNotificationsEnabled(enabled: Boolean) {
//        viewModelScope.launch { settingsRepo.setNotificationsEnabled(enabled) }
//    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setNotificationsEnabled(enabled)
            if (enabled) scheduler.scheduleDailyReminder()
            else scheduler.cancelDailyReminder()
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { settingsRepo.setLanguage(language) }
    }
}
