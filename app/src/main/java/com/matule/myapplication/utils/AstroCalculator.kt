package com.matule.myapplication.utils

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.constellation
import io.github.cosinekitty.astronomy.elongation
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.rotationEqjEcl
import io.github.cosinekitty.astronomy.searchRiseSet
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.math.roundToInt

enum class HemisphereVisibility {
    NORTHERN,
    SOUTHERN,
    BOTH,
    NOT_APPLICABLE
}

data class PlanetPosition(
    val rightAscension: Double? = null,
    val declination: Double? = null,
    val azimuth: Double? = null,
    val altitude: Double? = null,
    val isVisible: Boolean,
    val hasLocalSkyPosition: Boolean,
    val hemisphere: HemisphereVisibility = HemisphereVisibility.NOT_APPLICABLE,
    val distanceFromEarthAu: Double? = null,
    val distanceFromSunAu: Double,
    val heliocentricXAu: Double,
    val heliocentricYAu: Double,
    val heliocentricZAu: Double,
    val magnitude: Double? = null,
    val elongationDegrees: Double? = null,
    val constellationCode: String? = null,
    val constellationName: String? = null,
    val illuminationPercent: Int? = null,
    val riseTime: Date? = null,
    val setTime: Date? = null
)

class AstroCalculator(
    private val observerLatitude: Double = 55.7558,
    private val observerLongitude: Double = 37.6173,
    private val observerElevationMeters: Double = 156.0
) {

    private data class CacheKey(
        val planetName: String,
        val minuteBucket: Long,
        val observerLatitude: Double,
        val observerLongitude: Double,
        val observerElevationMeters: Double
    )

    private val eqjToEclRotation = rotationEqjEcl()
    private val positionCache = object : LinkedHashMap<CacheKey, PlanetPosition>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, PlanetPosition>?): Boolean {
            return size > 96
        }
    }

    fun calculatePlanetPosition(planetName: String, observationTime: Date = Date()): PlanetPosition {
        val normalizedName = planetName.trim().lowercase(Locale.ROOT)
        val cacheKey = CacheKey(
            planetName = normalizedName,
            minuteBucket = observationTime.time / 60_000L,
            observerLatitude = observerLatitude,
            observerLongitude = observerLongitude,
            observerElevationMeters = observerElevationMeters
        )

        synchronized(positionCache) {
            positionCache[cacheKey]
        }?.let { return it }

        val calculated = calculatePlanetPositionInternal(normalizedName, observationTime)
        synchronized(positionCache) {
            positionCache[cacheKey] = calculated
        }
        return calculated
    }

    fun visiblePlanets(planets: List<String>, observationTime: Date = Date()): List<String> {
        return planets.filter { planetName ->
            calculatePlanetPosition(planetName, observationTime).isVisible
        }
    }

    private fun calculatePlanetPositionInternal(
        normalizedPlanetName: String,
        observationTime: Date
    ): PlanetPosition {
        val body = mapPlanetNameToBody(normalizedPlanetName)
        val time = Time.fromMillisecondsSince1970(observationTime.time)
        val heliocentricVector = eqjToEclRotation.rotate(helioVector(body, time))
        val distanceFromSunAu = heliocentricVector.length()

        if (body == Body.Earth) {
            return PlanetPosition(
                isVisible = false,
                hasLocalSkyPosition = false,
                distanceFromEarthAu = 0.0,
                distanceFromSunAu = distanceFromSunAu,
                heliocentricXAu = heliocentricVector.x,
                heliocentricYAu = heliocentricVector.y,
                heliocentricZAu = heliocentricVector.z
            )
        }

        val observer = Observer(observerLatitude, observerLongitude, observerElevationMeters)
        val equatorial = equator(
            body,
            time,
            observer,
            EquatorEpoch.OfDate,
            Aberration.Corrected
        )
        val topocentric = horizon(
            time,
            observer,
            equatorial.ra,
            equatorial.dec,
            Refraction.Normal
        )
        val illumination = illumination(body, time)
        val elongation = elongation(body, time)
        val constellation = constellation(equatorial.ra, equatorial.dec)
        val distanceFromEarthAu = geoVector(body, time, Aberration.Corrected).length()

        return PlanetPosition(
            rightAscension = equatorial.ra,
            declination = equatorial.dec,
            azimuth = topocentric.azimuth,
            altitude = topocentric.altitude,
            isVisible = topocentric.altitude > 0.0,
            hasLocalSkyPosition = true,
            hemisphere = determineHemisphere(equatorial.dec),
            distanceFromEarthAu = distanceFromEarthAu,
            distanceFromSunAu = distanceFromSunAu,
            heliocentricXAu = heliocentricVector.x,
            heliocentricYAu = heliocentricVector.y,
            heliocentricZAu = heliocentricVector.z,
            magnitude = illumination.mag,
            elongationDegrees = elongation.elongation,
            constellationCode = constellation.symbol,
            constellationName = constellation.name,
            illuminationPercent = (illumination.phaseFraction * 100.0).roundToInt().coerceIn(0, 100),
            riseTime = searchRiseOrSet(body, observer, time, Direction.Rise),
            setTime = searchRiseOrSet(body, observer, time, Direction.Set)
        )
    }

    private fun searchRiseOrSet(
        body: Body,
        observer: Observer,
        time: Time,
        direction: Direction
    ): Date? {
        return runCatching {
            searchRiseSet(body, observer, direction, time, 2.0)?.let { eventTime ->
                Date(eventTime.toMillisecondsSince1970())
            }
        }.getOrNull()
    }

    private fun determineHemisphere(declination: Double): HemisphereVisibility {
        return when {
            declination > 0.25 -> HemisphereVisibility.NORTHERN
            declination < -0.25 -> HemisphereVisibility.SOUTHERN
            else -> HemisphereVisibility.BOTH
        }
    }

    private fun mapPlanetNameToBody(planetName: String): Body {
        return when (planetName) {
            "mercury", "меркурий" -> Body.Mercury
            "venus", "венера" -> Body.Venus
            "earth", "земля" -> Body.Earth
            "mars", "марс" -> Body.Mars
            "jupiter", "юпитер" -> Body.Jupiter
            "saturn", "сатурн" -> Body.Saturn
            "uranus", "уран" -> Body.Uranus
            "neptune", "нептун" -> Body.Neptune
            "pluto", "плутон" -> Body.Pluto
            else -> error("Unsupported planet name: $planetName")
        }
    }
}
