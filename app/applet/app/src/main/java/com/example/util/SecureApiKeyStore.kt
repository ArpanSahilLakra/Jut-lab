package com.example.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureApiKeyStore {
    private const val PREF_FILE = "jut_ece_secure_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    private fun getEncryptedPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    fun saveApiKey(context: Context, apiKey: String) {
        try {
            getEncryptedPrefs(context).edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
        } catch (e: Exception) {
        }
    }

    fun getApiKey(context: Context): String? {
        return try {
            getEncryptedPrefs(context).getString(KEY_GEMINI_API_KEY, null)
        } catch (e: Exception) {
            null
        }
    }

    fun removeApiKey(context: Context) {
        try {
            getEncryptedPrefs(context).edit().remove(KEY_GEMINI_API_KEY).apply()
        } catch (e: Exception) {
        }
    }

    fun hasApiKey(context: Context): Boolean {
        val key = getApiKey(context)
        return !key.isNullOrBlank()
    }
}
