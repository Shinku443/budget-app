package com.projects.shinku443.budgetapp.settings

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

actual class SecureKeyProvider(private val context: Context) {

    companion object {
        private const val KEY_ALIAS = "openai_key_alias"
        private const val PREFS_NAME = "budget_app_secure_prefs"
        private const val KEY_OPENAI_IV = "openai_api_key_iv"
        private const val KEY_OPENAI_ENCRYPTED = "openai_api_key_encrypted"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return iv to encrypted
    }

    private fun decrypt(iv: ByteArray, encrypted: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    actual fun saveApiKey(apiKey: String) {
        val (iv, encryptedApiKey) = encrypt(apiKey)
        prefs.edit()
            .putString(KEY_OPENAI_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .putString(KEY_OPENAI_ENCRYPTED, Base64.encodeToString(encryptedApiKey, Base64.DEFAULT))
            .apply()
    }

    actual fun getApiKey(): String? {
        val ivString = prefs.getString(KEY_OPENAI_IV, null)
        val encryptedApiKeyString = prefs.getString(KEY_OPENAI_ENCRYPTED, null)

        if (ivString != null && encryptedApiKeyString != null) {
            return try {
                val iv = Base64.decode(ivString, Base64.DEFAULT)
                val encryptedApiKey = Base64.decode(encryptedApiKeyString, Base64.DEFAULT)
                decrypt(iv, encryptedApiKey)
            } catch (e: Exception) {
                // Could fail if key is invalidated or data is corrupt
                null
            }
        }
        return null
    }
}
