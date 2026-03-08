package com.matule.myapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.matule.myapplication.models.ObservationPhoto
import com.matule.myapplication.models.UserObservation
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Random
import kotlin.collections.iterator

class ObservationRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("observations", Context.MODE_PRIVATE)
    private val photosDir = File(context.filesDir, "observation_photos")

    init {
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
    }

    // Сохранить наблюдение
    fun saveObservation(observation: UserObservation): Int {
        val id = if (observation.id == 0) {
            generateId()
        } else {
            observation.id
        }

        val updatedObservation = observation.copy(id = id)
        saveToPreferences(updatedObservation)
        return id
    }

    // Получить все наблюдения
    fun getAllObservations(): List<UserObservation> {
        val observations = mutableListOf<UserObservation>()
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith("observation_")) {
                val jsonString = value as? String
                jsonString?.let {
                    val obs = parseObservation(it)
                    obs?.let { observations.add(it) }
                }
            }
        }

        return observations.sortedByDescending { it.observationDate }
    }

    // Получить наблюдения по статусу
    fun getObservationsByStatus(status: String): List<UserObservation> {
        return getAllObservations().filter { it.status == status }
    }

    // Удалить наблюдение
    fun deleteObservation(id: Int): Boolean {
        val key = "observation_$id"
        if (prefs.contains(key)) {
            // Удаляем связанные фото
            deletePhotosForObservation(id)

            prefs.edit().remove(key).apply()
            return true
        }
        return false
    }

    // Добавить фото к наблюдению
    fun addPhoto(observationId: Int, bitmap: Bitmap, description: String? = null): ObservationPhoto? {
        return try {
            val fileName = "obs_${observationId}_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val photo = ObservationPhoto(
                observationId = observationId,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                description = description,
                uploadDate = Date()
            )

            savePhotoToPreferences(photo)
            photo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Получить все фото наблюдения
    fun getPhotosForObservation(observationId: Int): List<ObservationPhoto> {
        val photos = mutableListOf<ObservationPhoto>()
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith("photo_")) {
                val jsonString = value as? String
                jsonString?.let {
                    val photo = parsePhoto(it)
                    if (photo?.observationId == observationId) {
                        photos.add(photo)
                    }
                }
            }
        }

        return photos.sortedByDescending { it.uploadDate }
    }

    // Получить все фото
    fun getAllPhotos(): List<ObservationPhoto> {
        val photos = mutableListOf<ObservationPhoto>()
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith("photo_")) {
                val jsonString = value as? String
                jsonString?.let {
                    val photo = parsePhoto(it)
                    photo?.let { photos.add(it) }
                }
            }
        }

        return photos.sortedByDescending { it.uploadDate }
    }

    // Удалить фото
    fun deletePhoto(photoId: Int): Boolean {
        val key = "photo_$photoId"
        val photo = getPhotoById(photoId)

        photo?.let {
            // Удаляем физический файл
            val file = File(it.filePath)
            if (file.exists()) {
                file.delete()
            }
        }

        return if (prefs.contains(key)) {
            prefs.edit().remove(key).apply()
            true
        } else {
            false
        }
    }

    // Загрузить фото из файла
    fun loadPhoto(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Форматировать размер файла
    fun formatFileSize(bytes: Long): String {
        val sizes = arrayOf("B", "KB", "MB", "GB")
        var order = 0
        var size = bytes.toDouble()
        while (size >= 1024 && order < sizes.size - 1) {
            order++
            size /= 1024
        }
        return String.format("%.2f %s", size, sizes[order])
    }

    // Приватные методы
    private fun generateId(): Int {
        return System.currentTimeMillis().toInt() and 0x7fffffff
    }

    private fun generatePhotoId(): Int {
        return (System.currentTimeMillis() + Random().nextInt(10000)).toInt() and 0x7fffffff
    }

    private fun saveToPreferences(observation: UserObservation) {
        val json = observationToJson(observation)
        prefs.edit().putString("observation_${observation.id}", json).apply()
    }

    private fun savePhotoToPreferences(photo: ObservationPhoto) {
        val photoWithId = if (photo.id == 0) {
            photo.copy(id = generatePhotoId())
        } else {
            photo
        }

        val json = photoToJson(photoWithId)
        prefs.edit().putString("photo_${photoWithId.id}", json).apply()
    }

    private fun deletePhotosForObservation(observationId: Int) {
        val photos = getPhotosForObservation(observationId)
        photos.forEach { photo ->
            deletePhoto(photo.id)
        }
    }

    private fun getPhotoById(id: Int): ObservationPhoto? {
        val jsonString = prefs.getString("photo_$id", null)
        return jsonString?.let { parsePhoto(it) }
    }

    private fun observationToJson(obs: UserObservation): String {
        return """
            {
                "id": ${obs.id},
                "userName": "${escapeJson(obs.userName)}",
                "objectName": "${escapeJson(obs.objectName)}",
                "objectType": "${escapeJson(obs.objectType ?: "")}",
                "observationDate": ${obs.observationDate.time},
                "observationTime": "${escapeJson(obs.observationTime ?: "")}",
                "location": "${escapeJson(obs.location ?: "")}",
                "telescopeUsed": "${escapeJson(obs.telescopeUsed ?: "")}",
                "weatherConditions": "${escapeJson(obs.weatherConditions ?: "")}",
                "seeingRating": ${obs.seeingRating},
                "personalNotes": "${escapeJson(obs.personalNotes ?: "")}",
                "createdAt": ${obs.createdAt.time},
                "status": "${escapeJson(obs.status)}",
                "actualObservationDate": ${obs.actualObservationDate?.time ?: 0},
                "actualObservationNotes": "${escapeJson(obs.actualObservationNotes ?: "")}"
            }
        """.trimIndent()
    }

    private fun photoToJson(photo: ObservationPhoto): String {
        return """
            {
                "id": ${photo.id},
                "observationId": ${photo.observationId},
                "fileName": "${escapeJson(photo.fileName)}",
                "filePath": "${escapeJson(photo.filePath)}",
                "fileSize": ${photo.fileSize},
                "description": "${escapeJson(photo.description ?: "")}",
                "uploadDate": ${photo.uploadDate.time}
            }
        """.trimIndent()
    }

    private fun parseObservation(json: String): UserObservation? {
        return try {
            val id = extractInt(json, "id")
            val userName = extractString(json, "userName")
            val objectName = extractString(json, "objectName")
            val objectType = extractString(json, "objectType").takeIf { it.isNotEmpty() }
            val observationDate = Date(extractLong(json, "observationDate"))
            val observationTime = extractString(json, "observationTime").takeIf { it.isNotEmpty() }
            val location = extractString(json, "location").takeIf { it.isNotEmpty() }
            val telescopeUsed = extractString(json, "telescopeUsed").takeIf { it.isNotEmpty() }
            val weatherConditions = extractString(json, "weatherConditions").takeIf { it.isNotEmpty() }
            val seeingRating = extractInt(json, "seeingRating")
            val personalNotes = extractString(json, "personalNotes").takeIf { it.isNotEmpty() }
            val createdAt = Date(extractLong(json, "createdAt"))
            val status = extractString(json, "status")
            val actualObservationDate = extractLong(json, "actualObservationDate").takeIf { it > 0 }?.let {
                Date(it)
            }
            val actualObservationNotes = extractString(json, "actualObservationNotes").takeIf { it.isNotEmpty() }

            UserObservation(
                id = id,
                userName = userName,
                objectName = objectName,
                objectType = objectType,
                observationDate = observationDate,
                observationTime = observationTime,
                location = location,
                telescopeUsed = telescopeUsed,
                weatherConditions = weatherConditions,
                seeingRating = seeingRating,
                personalNotes = personalNotes,
                createdAt = createdAt,
                status = status,
                actualObservationDate = actualObservationDate,
                actualObservationNotes = actualObservationNotes,
                photos = emptyList() // Загружаем отдельно
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parsePhoto(json: String): ObservationPhoto? {
        return try {
            val id = extractInt(json, "id")
            val observationId = extractInt(json, "observationId")
            val fileName = extractString(json, "fileName")
            val filePath = extractString(json, "filePath")
            val fileSize = extractLong(json, "fileSize")
            val description = extractString(json, "description").takeIf { it.isNotEmpty() }
            val uploadDate = Date(extractLong(json, "uploadDate"))

            ObservationPhoto(
                id = id,
                observationId = observationId,
                fileName = fileName,
                filePath = filePath,
                fileSize = fileSize,
                description = description,
                uploadDate = uploadDate
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\"", "\\\"").replace("\n", "\\n")
    }

    private fun extractString(json: String, key: String): String {
        val pattern = "\"$key\":\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1) ?: ""
    }

    private fun extractInt(json: String, key: String): Int {
        val pattern = "\"$key\":(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun extractLong(json: String, key: String): Long {
        val pattern = "\"$key\":(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
    }
}