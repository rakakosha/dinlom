package com.matule.myapplication.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matule.myapplication.models.Planet
import com.matule.myapplication.models.UserObservation
import java.util.Date

fun createObservationFromPlanet(planet: Planet): UserObservation {
    return UserObservation(
        userName = "Астроном",
        objectName = planet.russianName,
        objectType = "Планета",
        observationDate = Date(),
        location = "Домашняя обсерватория",
        telescopeUsed = "Бинокль 10x50",
        weatherConditions = "Ясно",
        seeingRating = 3,
        personalNotes = buildString {
            append("Наблюдение ")
            append(planet.russianName)
            planet.funFact?.let {
                append(". Интересный факт: ")
                append(it)
            }
        }
    )
}

@Composable
fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
