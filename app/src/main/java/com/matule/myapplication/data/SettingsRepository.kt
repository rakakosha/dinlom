package com.matule.myapplication.data

import android.content.Context
import com.matule.myapplication.models.AppSettings

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        return AppSettings(
            theme = prefs.getString(KEY_THEME, "light") ?: "light",
            language = prefs.getString(KEY_LANGUAGE, "ru") ?: "ru",
            fontSize = prefs.getFloat(KEY_FONT_SIZE, 14f),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme)
            .putString(KEY_LANGUAGE, settings.language)
            .putFloat(KEY_FONT_SIZE, settings.fontSize)
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, settings.notificationsEnabled)
            .apply()
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
