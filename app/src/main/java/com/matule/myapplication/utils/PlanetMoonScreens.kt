package com.matule.myapplication.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.PlanetDatabase
import com.matule.myapplication.models.MoonPhaseInfo
import com.matule.myapplication.models.MoonPhaseType
import com.matule.myapplication.models.Planet
import java.util.Calendar
import java.util.Date

@Composable
fun MoonPhaseScreenContent(onBackClick: () -> Unit) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val phase = remember(selectedDate) { MoonPhaseCalculator.getMoonPhase(selectedDate) }
    val rollingPhases = remember(selectedDate) {
        buildList {
            val start = Calendar.getInstance().apply {
                time = selectedDate
                add(Calendar.DAY_OF_YEAR, -7)
            }
            repeat(15) {
                add(MoonPhaseCalculator.getMoonPhase(start.time))
                start.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Фазы Луны", "Moon phases"),
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = tr("Выбрать дату", "Pick date"),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = moonPhaseEmoji(phase.type), fontSize = 56.sp)
                        Text(
                            text = moonPhaseTitle(phase.type),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = formatDateTime(selectedDate, "dd MMMM yyyy"),
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = moonPhaseDescription(phase.type),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = tr("Освещенность", "Illumination") + ": ${phase.illumination}%",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = tr("Ближайшие ключевые фазы", "Upcoming key phases"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PhaseInfoRow(
                            tr("Новолуние", "New moon"),
                            MoonPhaseCalculator.getNextPhaseDate(selectedDate, MoonPhaseType.NEW_MOON)
                        )
                        PhaseInfoRow(
                            tr("Первая четверть", "First quarter"),
                            MoonPhaseCalculator.getNextPhaseDate(selectedDate, MoonPhaseType.FIRST_QUARTER)
                        )
                        PhaseInfoRow(
                            tr("Полнолуние", "Full moon"),
                            MoonPhaseCalculator.getNextPhaseDate(selectedDate, MoonPhaseType.FULL_MOON)
                        )
                        PhaseInfoRow(
                            tr("Последняя четверть", "Last quarter"),
                            MoonPhaseCalculator.getNextPhaseDate(selectedDate, MoonPhaseType.LAST_QUARTER)
                        )
                    }
                }
            }

            item {
                Text(
                    text = tr("Календарь на 15 дней", "15 day calendar"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(rollingPhases) { day ->
                        MoonDayCard(day)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { millis ->
                millis?.let { selectedDate = Date(it) }
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun PhaseInfoRow(label: String, date: Date) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(
            text = formatDateTime(date, "dd.MM.yyyy"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MoonDayCard(info: MoonPhaseInfo) {
    Card(shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = formatDateTime(info.date, "dd MMM"), fontWeight = FontWeight.Bold)
            Text(
                text = moonPhaseEmoji(info.type),
                fontSize = 28.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Text(text = "${info.illumination}%", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PlanetsScreenContent(
    onBackClick: () -> Unit,
    onPlanetClick: (Planet) -> Unit
) {
    val repository = LocalObservationRepository.current
    val calculator = LocalAstroCalculator.current
    val planets = remember { PlanetDatabase.getAllPlanets() }
    val minuteBucket = System.currentTimeMillis() / 60_000L
    var addedPlanetId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Планеты", "Planets"), onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(planets) { planet ->
                val position = remember(planet.id, minuteBucket) {
                    calculator.calculatePlanetPosition(planet.latinName)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlanetClick(planet) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${planet.russianName} (${planet.latinName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = planet.description,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(top = 6.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        when {
                                            !position.hasLocalSkyPosition -> tr("Точка отсчета", "Reference body")
                                            position.isVisible -> tr("Видна", "Visible")
                                            else -> tr("Ниже горизонта", "Below horizon")
                                        }
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = tr("Расстояние от Солнца", "Distance from Sun") +
                                ": ${formatAuValue(position.distanceFromSunAu)}"
                        )
                        Text(text = tr("Спутники", "Moons") + ": ${planet.moonsCount}")
                        DetailLine(
                            tr("Текущая высота", "Current altitude"),
                            if (position.hasLocalSkyPosition) formatAngleValue(position.altitude, 1)
                            else tr("не применяется", "n/a")
                        )
                        position.constellationCode?.let { code ->
                            DetailLine(
                                tr("Созвездие", "Constellation"),
                                buildString {
                                    append(code)
                                    position.constellationName?.let { name ->
                                        append(" (")
                                        append(name)
                                        append(")")
                                    }
                                }
                            )
                        }
                        position.magnitude?.let { magnitude ->
                            DetailLine(tr("Блеск", "Magnitude"), formatOptionalDouble(magnitude))
                        }
                        Text(
                            text = tr("Совет", "Tip") + ": ${planet.observationTips}",
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Button(
                            onClick = {
                                repository.saveObservation(createObservationFromPlanet(planet))
                                addedPlanetId = planet.id
                            },
                            modifier = Modifier.padding(top = 14.dp)
                        ) {
                            Text(
                                if (addedPlanetId == planet.id) tr("Добавлено", "Added")
                                else tr("Добавить в наблюдения", "Add to observations")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetDetailScreenContent(
    planet: Planet,
    onBackClick: () -> Unit
) {
    val repository = LocalObservationRepository.current
    val calculator = LocalAstroCalculator.current
    val position = remember(planet.id) {
        calculator.calculatePlanetPosition(planet.latinName)
    }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = "${planet.russianName} (${planet.latinName})",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(text = planet.description, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailLine(tr("Диаметр", "Diameter"), "${planet.diameterKm} км")
                        DetailLine(
                            tr("Расстояние от Солнца", "Distance from Sun"),
                            formatAuValue(position.distanceFromSunAu)
                        )
                        DetailLine(tr("Спутники", "Moons"), planet.moonsCount.toString())
                        planet.orbitalPeriodDays?.let {
                            DetailLine(tr("Период обращения", "Orbital period"), "$it ${tr("суток", "days")}")
                        }
                        planet.rotationPeriodHours?.let {
                            DetailLine(tr("Период вращения", "Rotation period"), "$it ${tr("часов", "hours")}")
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = tr("Положение в Солнечной системе", "Solar system position"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr(
                                "Гелиоцентрические эклиптические координаты, AU",
                                "Heliocentric ecliptic coordinates, AU"
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailLine("X", formatOptionalDouble(position.heliocentricXAu, 7))
                        DetailLine("Y", formatOptionalDouble(position.heliocentricYAu, 7))
                        DetailLine("Z", formatOptionalDouble(position.heliocentricZAu, 7))
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = tr("Положение на небе", "Sky position"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (position.hasLocalSkyPosition) {
                            DetailLine(
                                tr("Прямое восхождение", "Right ascension"),
                                formatHoursValue(position.rightAscension)
                            )
                            DetailLine(tr("Склонение", "Declination"), formatAngleValue(position.declination))
                            DetailLine(tr("Азимут", "Azimuth"), formatAngleValue(position.azimuth))
                            DetailLine(tr("Высота", "Altitude"), formatAngleValue(position.altitude))
                            DetailLine(tr("Полушарие", "Hemisphere"), hemisphereLabel(position.hemisphere))
                            DetailLine(
                                tr("Расстояние от Земли", "Distance from Earth"),
                                formatAuValue(position.distanceFromEarthAu)
                            )
                            position.constellationCode?.let { code ->
                                DetailLine(
                                    tr("Созвездие", "Constellation"),
                                    buildString {
                                        append(code)
                                        position.constellationName?.let { name ->
                                            append(" (")
                                            append(name)
                                            append(")")
                                        }
                                    }
                                )
                            }
                            position.magnitude?.let {
                                DetailLine(tr("Блеск", "Magnitude"), formatOptionalDouble(it))
                            }
                            position.elongationDegrees?.let {
                                DetailLine(tr("Элонгация", "Elongation"), formatAngleValue(it))
                            }
                            position.illuminationPercent?.let {
                                DetailLine(tr("Освещенность", "Illumination"), "$it%")
                            }
                            position.riseTime?.let {
                                DetailLine(tr("Восход", "Rise"), formatDateTime(it, "dd.MM HH:mm"))
                            }
                            position.setTime?.let {
                                DetailLine(tr("Заход", "Set"), formatDateTime(it, "dd.MM HH:mm"))
                            }
                        } else {
                            Text(
                                text = tr(
                                    "Для Земли локальные небесные координаты не рассчитываются, потому что наблюдатель находится на ней.",
                                    "Local sky coordinates are not calculated for Earth because the observer is already on it."
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            planet.funFact?.let { fact ->
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(text = tr("Интересный факт", "Fun fact"), fontWeight = FontWeight.Bold)
                            Text(text = fact, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = tr("Советы для наблюдения", "Observation tips"),
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = planet.observationTips, modifier = Modifier.padding(top = 8.dp))
                        Button(
                            onClick = {
                                repository.saveObservation(createObservationFromPlanet(planet))
                                saved = true
                            },
                            modifier = Modifier.padding(top = 14.dp)
                        ) {
                            Text(
                                if (saved) tr("Добавлено", "Added")
                                else tr("Добавить в наблюдения", "Add to observations")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun hemisphereLabel(hemisphere: HemisphereVisibility): String {
    return when (hemisphere) {
        HemisphereVisibility.NORTHERN -> tr("Северное", "Northern")
        HemisphereVisibility.SOUTHERN -> tr("Южное", "Southern")
        HemisphereVisibility.BOTH -> tr("Оба полушария", "Both hemispheres")
        HemisphereVisibility.NOT_APPLICABLE -> tr("Не применяется", "Not applicable")
    }
}
