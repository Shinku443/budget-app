package com.projects.shinku443.budgetapp.settings

actual class SettingsManager {
    private val secureKeyProvider = SecureKeyProvider()

    actual fun saveApiKey(apiKey: String) {
        secureKeyProvider.saveApiKey(apiKey)
    }
}
