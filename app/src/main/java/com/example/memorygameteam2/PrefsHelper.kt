package com.example.memorygameteam2

import android.content.Context

class PrefsHelper(context: Context) {
    private val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveUser(username: String, isPremium: Boolean) {
        sharedPref.edit().apply {
            putString("username", username)
            putBoolean("is_premium", isPremium)
            apply()
        }
    }

    fun getUsername(): String? = sharedPref.getString("username", null)
    fun isPremium(): Boolean = sharedPref.getBoolean("is_premium", false)
    fun clearUser() {
        sharedPref.edit().apply {
            remove("username")
            remove("is_premium")
            apply()
        }
    }
}