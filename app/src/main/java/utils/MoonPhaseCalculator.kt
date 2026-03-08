package com.matule.myapplication.utils



import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import com.matule.myapplication.models.MoonPhaseInfo
import com.matule.myapplication.models.MoonPhaseType
import com.matule.myapplication.utils.MoonPhaseCalculator
import java.text.SimpleDateFormat
import java.util.*

object MoonPhaseCalculator {
    private const val SYNODIC_MONTH = 29.530588853 // Средняя длина лунного цикла
    private val KNOWN_NEW_MOON = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2000)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 6)
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 14)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeZone = TimeZone.getTimeZone("UTC")
    }.time

    /**
     * Получить информацию о фазе Луны на указанную дату
     */
    fun getMoonPhase(date: Date): MoonPhaseInfo {
        // Переводим дату в UTC
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.time = date

        // Вычисляем количество дней с известного новолуния
        val daysSince = (utcCalendar.time.time - KNOWN_NEW_MOON.time) / (1000.0 * 60 * 60 * 24)

        // Текущая позиция в цикле (0-1)
        var cyclePosition = (daysSince % SYNODIC_MONTH) / SYNODIC_MONTH
        if (cyclePosition < 0) cyclePosition += 1.0

        // Освещенность (0-100%)
        val illumination = (0.5 * (1 - Math.cos(2 * Math.PI * cyclePosition)) * 100).toInt()

        // Возраст Луны в днях
        val age = cyclePosition * SYNODIC_MONTH

        // Определяем фазу
        val phaseType = when {
            cyclePosition < 0.03 || cyclePosition >= 0.97 -> MoonPhaseType.NEW_MOON
            cyclePosition < 0.22 -> MoonPhaseType.WAXING_CRESCENT
            cyclePosition < 0.28 -> MoonPhaseType.FIRST_QUARTER
            cyclePosition < 0.47 -> MoonPhaseType.WAXING_GIBBOUS
            cyclePosition < 0.53 -> MoonPhaseType.FULL_MOON
            cyclePosition < 0.72 -> MoonPhaseType.WANING_GIBBOUS
            cyclePosition < 0.78 -> MoonPhaseType.LAST_QUARTER
            else -> MoonPhaseType.WANING_CRESCENT
        }

        // Описание фазы
        val description = when (phaseType) {
            MoonPhaseType.NEW_MOON -> "Луна не видна"
            MoonPhaseType.WAXING_CRESCENT -> "Молодая луна"
            MoonPhaseType.FIRST_QUARTER -> "Освещена правая половина"
            MoonPhaseType.WAXING_GIBBOUS -> "Освещено больше половины"
            MoonPhaseType.FULL_MOON -> "Луна полностью освещена"
            MoonPhaseType.WANING_GIBBOUS -> "Освещено меньше половины"
            MoonPhaseType.LAST_QUARTER -> "Освещена левая половина"
            MoonPhaseType.WANING_CRESCENT -> "Старая луна"
        }

        return MoonPhaseInfo(
            type = phaseType,
            illumination = illumination,
            description = description,
            date = date,
            age = age
        )
    }

    /**
     * Получить дату следующей указанной фазы
     */
    fun getNextPhaseDate(afterDate: Date, targetPhase: MoonPhaseType): Date {
        val currentPhase = getMoonPhase(afterDate)
        val targetPhaseValue = targetPhase.ordinal * 0.125 // Каждая фаза занимает 1/8 цикла

        // Вычисляем текущую позицию в цикле
        val jd = toJulianDate(afterDate) - 2451550.1
        var currentCycle = jd / SYNODIC_MONTH
        currentCycle -= Math.floor(currentCycle)

        // Вычисляем сколько осталось до целевой фазы
        var delta = (targetPhaseValue - currentCycle + 1.0) % 1.0
        val daysToPhase = delta * SYNODIC_MONTH

        val calendar = Calendar.getInstance()
        calendar.time = afterDate
        calendar.add(Calendar.DAY_OF_YEAR, daysToPhase.toInt())
        calendar.add(Calendar.HOUR, ((daysToPhase - daysToPhase.toInt()) * 24).toInt())

        return calendar.time
    }

    /**
     * Получить фазы Луны на ближайшие N дней
     */
    fun getMoonPhasesForDays(startDate: Date, days: Int): List<MoonPhaseInfo> {
        val phases = mutableListOf<MoonPhaseInfo>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        for (i in 0 until days) {
            phases.add(getMoonPhase(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return phases
    }

    /**
     * Перевести дату в юлианскую
     */
    private fun toJulianDate(date: Date): Double {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY) +
                calendar.get(Calendar.MINUTE) / 60.0 +
                calendar.get(Calendar.SECOND) / 3600.0

        var y = year
        var m = month
        if (m < 3) {
            y--
            m += 12
        }

        val a = y / 100
        val b = 2 - a + a / 4

        return (365.25 * (y + 4716)).toInt() +
                (30.6001 * (m + 1)).toInt() +
                day + b - 1524.5 + hour / 24.0
    }
}