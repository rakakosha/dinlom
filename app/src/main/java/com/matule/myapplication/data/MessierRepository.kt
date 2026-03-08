package com.matule.myapplication.data

import android.content.Context
import com.matule.myapplication.models.MessierObject
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class LocalMessierRepository(context: Context) {

    private val allObjects: List<MessierObject> by lazy {
        loadFromAssets(context)
    }

    private fun loadFromAssets(context: Context): List<MessierObject> {
        val result = mutableListOf<MessierObject>()

        try {
            val inputStream = context.assets.open("messier_objects.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    MessierObject(
                        id = obj.getInt("id"),
                        messierNumber = obj.getString("messierNumber"),
                        ngcNumber = if (obj.has("ngcNumber") && !obj.isNull("ngcNumber") && obj.getString("ngcNumber").isNotEmpty())
                            obj.getString("ngcNumber") else null,
                        name = if (obj.has("name") && !obj.isNull("name") && obj.getString("name").isNotEmpty())
                            obj.getString("name") else null,
                        russianName = if (obj.has("russianName") && !obj.isNull("russianName") && obj.getString("russianName").isNotEmpty())
                            obj.getString("russianName") else null,
                        objectType = obj.getString("objectType"),
                        constellation = obj.getString("constellation"),
                        apparentMagnitude = obj.getDouble("apparentMagnitude"),
                        distanceLy = obj.getDouble("distanceLy"),
                        angularSize = obj.getString("angularSize"),
                        seasonVisibility = obj.getString("seasonVisibility"),
                        description = obj.getString("description"),
                        observationTips = obj.getString("observationTips")
                    )
                )
            }

            println("✅ Загружено ${result.size} объектов Мессье")
        } catch (e: Exception) {
            println("❌ Ошибка загрузки: ${e.message}")
            e.printStackTrace()
        }

        return result
    }

    fun searchObjects(searchTerm: String): List<MessierObject> {
        if (searchTerm.isBlank()) {
            return allObjects
        }

        val term = searchTerm.lowercase()

        return allObjects.filter { obj ->
            obj.messierNumber.lowercase().contains(term) ||
                    obj.russianName?.lowercase()?.contains(term) == true ||
                    obj.name?.lowercase()?.contains(term) == true ||
                    obj.ngcNumber?.lowercase()?.contains(term) == true ||
                    obj.objectType.lowercase().contains(term) ||
                    obj.constellation.lowercase().contains(term)
        }
    }

    fun getObjectById(id: Int): MessierObject? {
        return allObjects.find { it.id == id }
    }
}