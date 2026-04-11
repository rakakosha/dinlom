package com.matule.myapplication.models

enum class Screen {
    HOME,
    MOON_PHASE,
    TELESCOPE_GUIDE,
    MESSIER_SEARCH,
    PLANETS,
    VIDEOS,
    OBSERVATIONS,
    PHOTOS,
    SOCIAL,
    SETTINGS;

    val displayName: String
        get() = when (this) {
            HOME -> "Главная"
            MOON_PHASE -> "Фазы Луны"
            TELESCOPE_GUIDE -> "Памятка по телескопам"
            MESSIER_SEARCH -> "Поиск Мессье"
            PLANETS -> "Планеты"
            VIDEOS -> "Видео"
            OBSERVATIONS -> "Наблюдения"
            PHOTOS -> "Фотографии"
            SOCIAL -> "Соцсети"
            SETTINGS -> "Настройки"
        }

    val icon: String
        get() = when (this) {
            HOME -> "⌂"
            MOON_PHASE -> "◑"
            TELESCOPE_GUIDE -> "⌕"
            MESSIER_SEARCH -> "⌘"
            PLANETS -> "🪐"
            VIDEOS -> "▶"
            OBSERVATIONS -> "✎"
            PHOTOS -> "📷"
            SOCIAL -> "✦"
            SETTINGS -> "⚙"
        }
}
