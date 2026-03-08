package com.matule.myapplication.models

data class Planet(
    val id: Int,
    val russianName: String,
    val latinName: String,
    val distanceFromSunAu: Double, // Расстояние в а.е.
    val diameterKm: Double, // Диаметр в км
    val moonsCount: Int, // Количество спутников
    val observationTips: String, // Рекомендации по наблюдению
    val description: String // Описание планеты
)