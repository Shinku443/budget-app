package com.projects.shinku443.budget_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define theme options
enum class AppTheme { Light, Dark }

class SettingsViewModel : ViewModel() {

    // Theme state
    private val _theme = MutableStateFlow(AppTheme.Light)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    // Notifications state
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // Update theme
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            _theme.value = theme
            // TODO: persist to datastore/preferences if needed
        }
    }

    // Update notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            // TODO: persist to datastore/preferences if needed
        }
    }
}
