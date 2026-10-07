package com.rehelp.app.data

import android.content.Context

// Keeps the logged-in user in SharedPreferences
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("rehelp_session", Context.MODE_PRIVATE)

    fun saveLogin(user: User) {
        prefs.edit()
            .putInt("userId", user.userId)
            .putString("name", user.name)
            .putString("role", user.role)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getInt("userId", -1) != -1
    fun getUserId(): Int = prefs.getInt("userId", -1)
    fun getName(): String = prefs.getString("name", "") ?: ""
    fun getRole(): String = prefs.getString("role", "") ?: ""

    fun logout() {
        prefs.edit().clear().apply()
    }
}
