package com.matule.myapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import android.webkit.MimeTypeMap
import com.matule.myapplication.models.ObservationPhoto
import com.matule.myapplication.models.UserObservation
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID

class ObservationRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("observations", Context.MODE_PRIVATE)
    private val photosDir = File(context.filesDir, "observation_photos")
    private val photoCache = object : LruCache<String, Bitmap>(calculateCacheSizeInKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    init {
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
    }

    fun saveObservation(observation: UserObservation): Int {
        val id = if (observation.id == 0) generateId() else observation.id
        val updatedObservation = observation.copy(id = id)
        prefs.edit()
            .putString("observation_$id", observationToJson(updatedObservation))
            .apply()
        return id
    }

    fun getAllObservations(): List<UserObservation> {
        val photosByObservationId = getAllPhotos().groupBy { it.observationId }
        return prefs.all
            .filterKeys { it.startsWith("observation_") }
            .mapNotNull { (_, value) ->
                (value as? String)?.let { parseObservation(it, photosByObservationId) }
            }
            .sortedByDescending { it.observationDate.time }
    }

    fun getObservationById(id: Int): UserObservation? {
        val photosByObservationId = getAllPhotos().groupBy { it.observationId }
        return prefs.getString("observation_$id", null)?.let { parseObservation(it, photosByObservationId) }
    }

    fun getObservationsByStatus(status: String): List<UserObservation> {
        return getAllObservations().filter { it.status == status }
    }

    fun deleteObservation(id: Int): Boolean {
        val key = "observation_$id"
        if (!prefs.contains(key)) return false

        deletePhotosForObservation(id)
        prefs.edit().remove(key).apply()
        return true
    }

    fun addPhoto(observationId: Int, bitmap: Bitmap, description: String? = null): ObservationPhoto? {
        return try {
            val fileName = "obs_${observationId}_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            savePhotoToPreferences(
                ObservationPhoto(
                    observationId = observationId,
                    fileName = fileName,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    description = description,
                    uploadDate = Date()
                )
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun addPhotoFromUri(observationId: Int, uri: Uri, description: String? = null): ObservationPhoto? {
        return try {
            val extension = resolveExtension(uri)
            val fileName = "obs_${observationId}_${UUID.randomUUID()}.$extension"
            val file = File(photosDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            savePhotoToPreferences(
                ObservationPhoto(
                    observationId = observationId,
                    fileName = fileName,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    description = description,
                    uploadDate = Date()
                )
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun getPhotosForObservation(observationId: Int): List<ObservationPhoto> {
        return getAllPhotos()
            .filter { it.observationId == observationId }
            .sortedByDescending { it.uploadDate.time }
    }

    fun getAllPhotos(): List<ObservationPhoto> {
        return prefs.all
            .filterKeys { it.startsWith("photo_") }
            .mapNotNull { (_, value) -> (value as? String)?.let(::parsePhoto) }
            .sortedByDescending { it.uploadDate.time }
    }

    fun deletePhoto(photoId: Int): Boolean {
        val key = "photo_$photoId"
        if (!prefs.contains(key)) return false

        getPhotoById(photoId)?.let { photo ->
            val file = File(photo.filePath)
            if (file.exists()) {
                file.delete()
            }
        }

        prefs.edit().remove(key).apply()
        photoCache.evictAll()
        return true
    }

    fun loadPhoto(filePath: String, maxSize: Int = DEFAULT_PHOTO_MAX_SIZE): Bitmap? {
        return loadScaledPhoto(filePath, maxSize)
    }

    fun loadPhotoThumbnail(filePath: String, maxSize: Int = DEFAULT_THUMBNAIL_MAX_SIZE): Bitmap? {
        return loadScaledPhoto(filePath, maxSize)
    }

    private fun loadScaledPhoto(filePath: String, maxSize: Int): Bitmap? {
        return try {
            val cacheKey = "$filePath#$maxSize"
            photoCache.get(cacheKey)?.let { return it }

            val file = File(filePath)
            if (!file.exists()) return null

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSize)
                inPreferredConfig =
                    if (maxSize <= DEFAULT_THUMBNAIL_MAX_SIZE) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            }

            BitmapFactory.decodeFile(filePath, decodeOptions)?.also { photoCache.put(cacheKey, it) }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun formatFileSize(bytes: Long): String {
        val sizes = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var order = 0
        while (size >= 1024 && order < sizes.lastIndex) {
            size /= 1024
            order++
        }
        return String.format("%.2f %s", size, sizes[order])
    }

    private fun generateId(): Int {
        return (System.currentTimeMillis() and 0x7fffffff).toInt()
    }

    private fun generatePhotoId(): Int {
        return ((System.currentTimeMillis() + (Math.random() * 10_000).toLong()) and 0x7fffffff).toInt()
    }

    private fun savePhotoToPreferences(photo: ObservationPhoto): ObservationPhoto {
        val savedPhoto = if (photo.id == 0) photo.copy(id = generatePhotoId()) else photo
        prefs.edit()
            .putString("photo_${savedPhoto.id}", photoToJson(savedPhoto))
            .apply()
        return savedPhoto
    }

    private fun deletePhotosForObservation(observationId: Int) {
        getPhotosForObservation(observationId).forEach { photo ->
            deletePhoto(photo.id)
        }
    }

    private fun getPhotoById(id: Int): ObservationPhoto? {
        return prefs.getString("photo_$id", null)?.let(::parsePhoto)
    }

    private fun observationToJson(observation: UserObservation): String {
        return JSONObject().apply {
            put("id", observation.id)
            put("userName", observation.userName)
            put("objectName", observation.objectName)
            put("objectType", observation.objectType)
            put("observationDate", observation.observationDate.time)
            put("observationTime", observation.observationTime)
            put("location", observation.location)
            put("telescopeUsed", observation.telescopeUsed)
            put("weatherConditions", observation.weatherConditions)
            put("seeingRating", observation.seeingRating)
            put("personalNotes", observation.personalNotes)
            put("createdAt", observation.createdAt.time)
            put("status", observation.status)
            put("actualObservationDate", observation.actualObservationDate?.time)
            put("actualObservationNotes", observation.actualObservationNotes)
        }.toString()
    }

    private fun photoToJson(photo: ObservationPhoto): String {
        return JSONObject().apply {
            put("id", photo.id)
            put("observationId", photo.observationId)
            put("fileName", photo.fileName)
            put("filePath", photo.filePath)
            put("fileSize", photo.fileSize)
            put("description", photo.description)
            put("uploadDate", photo.uploadDate.time)
        }.toString()
    }

    private fun parseObservation(
        json: String,
        photosByObservationId: Map<Int, List<ObservationPhoto>>? = null
    ): UserObservation? {
        return try {
            val objectJson = JSONObject(json)
            val observationId = objectJson.optInt("id")
            UserObservation(
                id = observationId,
                userName = objectJson.optString("userName"),
                objectName = objectJson.optString("objectName"),
                objectType = objectJson.optString("objectType").takeIf { it.isNotBlank() },
                observationDate = Date(objectJson.optLong("observationDate")),
                observationTime = objectJson.optString("observationTime").takeIf { it.isNotBlank() },
                location = objectJson.optString("location").takeIf { it.isNotBlank() },
                telescopeUsed = objectJson.optString("telescopeUsed").takeIf { it.isNotBlank() },
                weatherConditions = objectJson.optString("weatherConditions").takeIf { it.isNotBlank() },
                seeingRating = objectJson.optInt("seeingRating", 3).coerceIn(1, 5),
                personalNotes = objectJson.optString("personalNotes").takeIf { it.isNotBlank() },
                createdAt = Date(objectJson.optLong("createdAt", System.currentTimeMillis())),
                status = objectJson.optString("status", "planned"),
                actualObservationDate = objectJson.optLong("actualObservationDate")
                    .takeIf { it > 0L }
                    ?.let(::Date),
                actualObservationNotes = objectJson.optString("actualObservationNotes").takeIf { it.isNotBlank() },
                photos = photosByObservationId?.get(observationId).orEmpty()
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    private fun parsePhoto(json: String): ObservationPhoto? {
        return try {
            val objectJson = JSONObject(json)
            ObservationPhoto(
                id = objectJson.optInt("id"),
                observationId = objectJson.optInt("observationId"),
                fileName = objectJson.optString("fileName"),
                filePath = objectJson.optString("filePath"),
                fileSize = objectJson.optLong("fileSize"),
                description = objectJson.optString("description").takeIf { it.isNotBlank() },
                uploadDate = Date(objectJson.optLong("uploadDate", System.currentTimeMillis()))
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    private fun resolveExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        val extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (!extensionFromMime.isNullOrBlank()) {
            return extensionFromMime
        }

        val path = uri.lastPathSegment.orEmpty()
        val dotIndex = path.lastIndexOf('.')
        return if (dotIndex >= 0 && dotIndex < path.lastIndex) {
            path.substring(dotIndex + 1)
        } else {
            "jpg"
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sampleSize = 1
        var currentWidth = width
        var currentHeight = height

        while (currentWidth > maxSize || currentHeight > maxSize) {
            currentWidth /= 2
            currentHeight /= 2
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun calculateCacheSizeInKb(): Int {
        val maxMemoryInKb = (Runtime.getRuntime().maxMemory() / 1024L).toInt()
        return (maxMemoryInKb / 8).coerceAtLeast(2 * 1024)
    }

    companion object {
        private const val DEFAULT_PHOTO_MAX_SIZE = 1_920
        private const val DEFAULT_THUMBNAIL_MAX_SIZE = 720
    }
}
