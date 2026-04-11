package com.matule.myapplication.utils

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

data class PlanetPosition(
    val rightAscension: Double,
    val declination: Double,
    val azimuth: Double,
    val altitude: Double,
    val isVisible: Boolean,
    val hemisphere: String,
    val distanceFromEarthAu: Double,
    val phase: String? = null,
    val riseTime: Date? = null,
    val setTime: Date? = null
)

class AstroCalculator(
    private val observerLatitude: Double = 55.7558,
    private val observerLongitude: Double = 37.6173,
    private val observationTime: Date = Date()
) {

    fun calculatePlanetPosition(planetName: String): PlanetPosition {
        val julianDate = toJulianDate(observationTime)

        val basePosition = when (planetName.lowercase(Locale.ROOT)) {
            "mercury" -> calculateInnerPlanet(julianDate, 0.387, 0.2056)
                .copy(hemisphere = "Все полушария")
            "venus" -> calculateInnerPlanet(julianDate, 0.723, 0.0067)
                .copy(
                    hemisphere = calculateHemisphere(calculateInnerPlanet(julianDate, 0.723, 0.0067).declination),
                    phase = calculateVenusPhase(julianDate)
                )
            "mars" -> calculateOuterPlanet(julianDate, 1.52, 0.0934)
                .copy(hemisphere = calculateHemisphere(calculateOuterPlanet(julianDate, 1.52, 0.0934).declination))
            "jupiter" -> calculateOuterPlanet(julianDate, 5.20, 0.0489)
            "saturn" -> calculateOuterPlanet(julianDate, 9.58, 0.0565)
            "uranus" -> calculateOuterPlanet(julianDate, 19.20, 0.0463)
            "neptune" -> calculateOuterPlanet(julianDate, 30.05, 0.0094)
            else -> calculateInnerPlanet(julianDate, 1.0, 0.0167)
        }

        return calculateVisibility(basePosition)
    }

    fun visiblePlanets(planets: List<String>): List<String> {
        return planets.filter { calculatePlanetPosition(it).isVisible }
    }

    private fun calculateHemisphere(declination: Double): String {
        return if (declination >= 0) "Северное" else "Южное"
    }

    private fun calculateVenusPhase(julianDate: Double): String {
        val phaseAngle = (julianDate % 584) / 584 * 360
        val illumination = (1 + cos(Math.toRadians(phaseAngle))) / 2 * 100

        return when {
            illumination > 90 -> "Полная"
            illumination > 70 -> "Выпуклая"
            illumination > 30 -> "Полумесяц"
            else -> "Серп"
        }
    }

    private fun calculateVisibility(position: PlanetPosition): PlanetPosition {
        val isVisible = position.altitude > 0
        val calendar = Calendar.getInstance()
        calendar.time = observationTime

        val riseTime = if (isVisible) {
            calendar.clone().let {
                it as Calendar
                it.add(Calendar.HOUR_OF_DAY, -6)
                it.time
            }
        } else {
            null
        }

        val setTime = if (isVisible) {
            calendar.clone().let {
                it as Calendar
                it.add(Calendar.HOUR_OF_DAY, 6)
                it.time
            }
        } else {
            null
        }

        return position.copy(
            isVisible = isVisible,
            riseTime = riseTime,
            setTime = setTime
        )
    }

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
        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = y / 100
        val b = 2 - a + a / 4

        return (365.25 * (y + 4716)).toInt() +
            (30.6001 * (m + 1)).toInt() +
            day + b - 1524.5 + hour / 24.0
    }

    private fun calculateInnerPlanet(jd: Double, semiMajorAxis: Double, eccentricity: Double): PlanetPosition {
        val declination = sin(Math.toRadians(jd)) * 20
        return PlanetPosition(
            rightAscension = (jd % 360) / 15,
            declination = declination,
            azimuth = (jd * 0.5 + observerLongitude * 0.1) % 360,
            altitude = 30 + sin(jd * 0.1) * (35 + observerLatitude / 10),
            isVisible = false,
            hemisphere = calculateHemisphere(declination),
            distanceFromEarthAu = semiMajorAxis * (1 + eccentricity * cos(Math.toRadians(jd)))
        )
    }

    private fun calculateOuterPlanet(jd: Double, semiMajorAxis: Double, eccentricity: Double): PlanetPosition {
        val declination = sin(Math.toRadians(jd * 0.7)) * 18
        return PlanetPosition(
            rightAscension = ((jd * 0.7) % 360) / 15,
            declination = declination,
            azimuth = (jd * 0.35 + observerLongitude * 0.2) % 360,
            altitude = 20 + sin(jd * 0.05 + observerLatitude) * 28,
            isVisible = false,
            hemisphere = calculateHemisphere(declination),
            distanceFromEarthAu = semiMajorAxis * (1 + eccentricity * cos(Math.toRadians(jd * 0.4)))
        )
    }
}
