package com.matule.myapplication.models

/**
 * Перечисление всех экранов приложения для навигации
 */
enum class Screen {
    HOME,               // Главный экран
    MOON_PHASE,         // Фазы Луны
    TELESCOPE_GUIDE,    // Памятка по телескопам
    MESSIER_SEARCH,     // Поиск объектов Мессье
    PLANETS,            // Планеты
    VIDEOS,             // Видео о звёздах
    OBSERVATIONS,       // Мои наблюдения
    SOCIAL,             // Соцсети
    SETTINGS;           // Настройки

    /**
     * Возвращает русское название экрана для отображения в UI
     */
    val displayName: String
        get() = when (this) {
            HOME -> "Главная"
            MOON_PHASE -> "Фазы Луны"
            TELESCOPE_GUIDE -> "Памятка по телескопам"
            MESSIER_SEARCH -> "Поиск Мессье"
            PLANETS -> "Планеты"
            VIDEOS -> "Видео о звёздах"
            OBSERVATIONS -> "Мои наблюдения"
            SOCIAL -> "Соцсети"
            SETTINGS -> "Настройки"
        }

    /**
     * Возвращает иконку для экрана (эмодзи)
     */
    val icon: String
        get() = when (this) {
            HOME -> "🏠"
            MOON_PHASE -> "🌙"
            TELESCOPE_GUIDE -> "🔭"
            MESSIER_SEARCH -> "🔍"
            PLANETS -> "🪐"
            VIDEOS -> "📹"
            OBSERVATIONS -> "📝"
            SOCIAL -> "👥"
            SETTINGS -> "⚙️"
        }
}