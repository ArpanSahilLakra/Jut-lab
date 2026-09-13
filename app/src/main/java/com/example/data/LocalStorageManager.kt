package com.example.data

import android.content.Context
import android.content.SharedPreferences

class LocalStorageManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("jut_virtual_lab_v1", Context.MODE_PRIVATE)

  fun saveData(key: String, value: String) {
    prefs.edit().putString(key, value).apply()
  }

  fun getData(key: String, defaultValue: String = ""): String {
    return prefs.getString(key, defaultValue) ?: defaultValue
  }

  fun removeData(key: String) {
    prefs.edit().remove(key).apply()
  }

  fun clearAll() {
    prefs.edit().clear().apply()
  }
}
