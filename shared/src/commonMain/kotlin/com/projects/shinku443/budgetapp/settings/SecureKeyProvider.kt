package com.projects.shinku443.budgetapp.settings

expect class SecureKeyProvider {
    fun getApiKey(): String?
    fun saveApiKey(apiKey: String)
}
