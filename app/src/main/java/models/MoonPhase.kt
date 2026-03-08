package com.matule.myapplication.models
import java.util.*

enum class MoonPhaseType {
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT;

    // Русское название фазы
    val displayName: String
        get() = when (this) {
            MoonPhaseType.NEW_MOON -> "Новолуние"
            MoonPhaseType.WAXING_CRESCENT -> "Растущий серп"
            MoonPhaseType.FIRST_QUARTER -> "Первая четверть"
            MoonPhaseType.WAXING_GIBBOUS -> "Растущая Луна"
            MoonPhaseType.FULL_MOON -> "Полнолуние"
            MoonPhaseType.WANING_GIBBOUS -> "Убывающая Луна"
            MoonPhaseType.LAST_QUARTER -> "Последняя четверть"
            MoonPhaseType.WANING_CRESCENT -> "Старый серп"
        }

    // Эмодзи для фазы
    val emoji: String
        get() = when (this) {
            MoonPhaseType.NEW_MOON -> "🌑"
            MoonPhaseType.WAXING_CRESCENT -> "🌒"
            MoonPhaseType.FIRST_QUARTER -> "🌓"
            MoonPhaseType.WAXING_GIBBOUS -> "🌔"
            MoonPhaseType.FULL_MOON -> "🌕"
            MoonPhaseType.WANING_GIBBOUS -> "🌖"
            MoonPhaseType.LAST_QUARTER -> "🌗"
            MoonPhaseType.WANING_CRESCENT -> "🌘"
        }
}

data class MoonPhaseInfo(
    val type: MoonPhaseType,
    val illumination: Int, // 0-100%
    val description: String,
    val date: Date,
    val age: Double // Возраст Луны в днях
)