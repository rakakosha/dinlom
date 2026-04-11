package com.matule.myapplication.models

data class Planet(
    val id: Int,
    val russianName: String,
    val latinName: String,
    val distanceFromSunAu: Double,
    val diameterKm: Double,
    val moonsCount: Int,
    val observationTips: String,
    val description: String,
    val orbitalPeriodDays: Double? = null,
    val rotationPeriodHours: Double? = null,
    val funFact: String? = null
)
