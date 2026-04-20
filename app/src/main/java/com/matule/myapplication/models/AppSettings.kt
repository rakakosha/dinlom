package com.matule.myapplication.models

data class AppSettings(
    val theme: String = "light",
    val language: String = "ru",
    val fontSize: Float = 14f,
    val notificationsEnabled: Boolean = true
) {
    val fontScale: Float
        get() = (fontSize / 14f).coerceIn(0.85f, 1.5f)
}
