package com.matule.myapplication.models

enum class Screen {
    HOME,
    ASTRO_NEWS,
    MOON_PHASE,
    TELESCOPE_GUIDE,
    MESSIER_SEARCH,
    PLANETS,
    VIDEOS,
    OBSERVATIONS,
    PHOTOS,
    SOCIAL,
    REGISTRATION,
    SETTINGS;

    val displayName: String
        get() = when (this) {
            HOME -> "Главная"
            ASTRO_NEWS -> "Астроновости"
            MOON_PHASE -> "Фазы Луны"
            TELESCOPE_GUIDE -> "Памятка по телескопам"
            MESSIER_SEARCH -> "Поиск Мессье"
            PLANETS -> "Планеты"
            VIDEOS -> "Видео"
            OBSERVATIONS -> "Наблюдения"
            PHOTOS -> "Фотографии"
            SOCIAL -> "Соцсети"
            REGISTRATION -> "Регистрация"
            SETTINGS -> "Настройки"
        }

    val icon: String
        get() = when (this) {
            HOME -> "⌂"
            ASTRO_NEWS -> "📰"
            MOON_PHASE -> "◑"
            TELESCOPE_GUIDE -> "⌕"
            MESSIER_SEARCH -> "⌘"
            PLANETS -> "🪐"
            VIDEOS -> "▶"
            OBSERVATIONS -> "✎"
            PHOTOS -> "📷"
            SOCIAL -> "✦"
            REGISTRATION -> "👤"
            SETTINGS -> "⚙"
        }
}
