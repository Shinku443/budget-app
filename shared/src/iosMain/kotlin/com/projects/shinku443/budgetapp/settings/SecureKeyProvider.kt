package com.projects.shinku443.budgetapp.settings

import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import platform.posix.memcpy

actual class SecureKeyProvider {
    private val service = "com.projects.shinku443.budgetapp"
    private val account = "openai_api_key"

    actual fun getApiKey(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
            kSecReturnData to true
        )

        var item: Any? = null
        val status: OSStatus = SecItemCopyMatching(query as CFDictionaryRef, item.ptr)

        if (status == 0) {
            val data = item as NSData
            val bytes = data.bytes
            val length = data.length
            val byteArray = ByteArray(length.toInt())
            memcpy(byteArray.refTo(0), bytes, length)
            return byteArray.decodeToString()
        }

        return null
    }

    actual fun saveApiKey(apiKey: String) {
        val data = apiKey.encodeToByteArray()

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account
        )
        SecItemDelete(query as CFDictionaryRef)

        val newQuery = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
            kSecValueData to data
        )
        SecItemAdd(newQuery as CFDictionaryRef, null)
    }
}
