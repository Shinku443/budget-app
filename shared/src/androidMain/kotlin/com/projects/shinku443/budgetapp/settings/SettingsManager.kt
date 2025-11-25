package com.projects.shinku443.budgetapp.settings

import android.content.Context

actual class SettingsManager(private val context: Context) {
    private val secureKeyProvider: SecureKeyProvider = SecureKeyProvider(context)

    actual fun saveApiKey(apiKey: String) {
        secureKeyProvider.saveApiKey(apiKey)
    }
}
