package com.matule.myapplication.models

import java.util.Date

data class UserObservation(
    val id: Int = 0,
    val userName: String = "",
    val objectName: String,
    val objectType: String? = null,
    val observationDate: Date, // Плановая дата
    val observationTime: String? = null,
    val location: String? = null,
    val telescopeUsed: String? = null,
    val weatherConditions: String? = null,
    val seeingRating: Int = 3, // 1-5
    val personalNotes: String? = null,
    val createdAt: Date = Date(),

    // Новые поля
    val status: String = "planned", // planned, completed, cancelled
    val actualObservationDate: Date? = null,
    val actualObservationNotes: String? = null,
    val photos: List<ObservationPhoto> = emptyList()
)

data class ObservationPhoto(
    val id: Int = 0,
    val observationId: Int,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val description: String? = null,
    val uploadDate: Date = Date()
)