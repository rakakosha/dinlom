package com.matule.myapplication.models

data class MessierObject(
    val id: Int,
    val messierNumber: String,
    val ngcNumber: String?,
    val name: String?,
    val russianName: String?,
    val objectType: String,
    val constellation: String,
    val apparentMagnitude: Double,
    val distanceLy: Double,
    val angularSize: String,
    val seasonVisibility: String,
    val description: String,
    val observationTips: String
)