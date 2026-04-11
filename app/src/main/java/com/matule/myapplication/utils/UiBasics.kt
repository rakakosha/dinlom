package com.matule.myapplication.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.PlanetDatabase
import com.matule.myapplication.models.AppSettings
import com.matule.myapplication.models.MoonPhaseType
import com.matule.myapplication.models.Screen
import java.util.Date

val LocalAppSettings = compositionLocalOf { AppSettings() }

@Composable
fun tr(ru: String, en: String): String {
    return if (LocalAppSettings.current.language == "en") en else ru
}

fun moonPhaseTitle(type: MoonPhaseType): String {
    return when (type) {
        MoonPhaseType.NEW_MOON -> "Новолуние"
        MoonPhaseType.WAXING_CRESCENT -> "Растущий серп"
        MoonPhaseType.FIRST_QUARTER -> "Первая четверть"
        MoonPhaseType.WAXING_GIBBOUS -> "Растущая Луна"
        MoonPhaseType.FULL_MOON -> "Полнолуние"
        MoonPhaseType.WANING_GIBBOUS -> "Убывающая Луна"
        MoonPhaseType.LAST_QUARTER -> "Последняя четверть"
        MoonPhaseType.WANING_CRESCENT -> "Старый серп"
    }
}

fun moonPhaseEmoji(type: MoonPhaseType): String {
    return when (type) {
        MoonPhaseType.NEW_MOON -> "🌑"
        MoonPhaseType.WAXING_CRESCENT -> "🌒"
        MoonPhaseType.FIRST_QUARTER -> "🌓"
        MoonPhaseType.WAXING_GIBBOUS -> "🌔"
        MoonPhaseType.FULL_MOON -> "🌕"
        MoonPhaseType.WANING_GIBBOUS -> "🌖"
        MoonPhaseType.LAST_QUARTER -> "🌗"
        MoonPhaseType.WANING_CRESCENT -> "🌘"
    }
}

fun moonPhaseDescription(type: MoonPhaseType): String {
    return when (type) {
        MoonPhaseType.NEW_MOON -> "Луна не видна."
        MoonPhaseType.WAXING_CRESCENT -> "Молодая Луна хорошо заметна вечером."
        MoonPhaseType.FIRST_QUARTER -> "Освещена правая половина диска."
        MoonPhaseType.WAXING_GIBBOUS -> "Освещено больше половины поверхности."
        MoonPhaseType.FULL_MOON -> "Полностью освещенный лунный диск."
        MoonPhaseType.WANING_GIBBOUS -> "После полнолуния освещенность уменьшается."
        MoonPhaseType.LAST_QUARTER -> "Освещена левая половина диска."
        MoonPhaseType.WANING_CRESCENT -> "Старая Луна перед новым циклом."
    }
}

@Composable
fun TopBarContent(
    currentDateTime: String,
    onMenuClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tr("Астрономический гид", "Astronomy Guide"),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = currentDateTime,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                modifier = Modifier.size(46.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = tr("Меню", "Menu"),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Назад", "Back"),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = title,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenContent(onNavigate: (Screen) -> Unit) {
    val currentMoonPhase = remember { MoonPhaseCalculator.getMoonPhase(Date()) }
    val visiblePlanets = remember {
        val calculator = AstroCalculator()
        PlanetDatabase.getAllPlanets()
            .filter { it.latinName != "Earth" }
            .filter { calculator.calculatePlanetPosition(it.latinName).isVisible }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = tr("Добро пожаловать в Астрономический гид", "Welcome to Astronomy Guide"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr(
                            "Здесь собраны планеты, фазы Луны, объекты Мессье, наблюдения и полезные советы.",
                            "Planets, Moon phases, Messier objects, observations and practical tips are gathered here."
                        ),
                        modifier = Modifier.padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    androidx.compose.material3.Button(
                        onClick = { onNavigate(Screen.PLANETS) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(tr("Открыть планеты", "Open planets"))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = tr("Сегодня на небе", "Tonight's sky"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (visiblePlanets.isEmpty()) {
                            tr("Сейчас яркие планеты ниже горизонта.", "Bright planets are below the horizon right now.")
                        } else {
                            visiblePlanets.joinToString(
                                prefix = tr("Сейчас видны: ", "Visible now: ")
                            ) { it.russianName }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { onNavigate(Screen.PLANETS) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(tr("Подробнее", "Details"))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = tr("Фаза Луны", "Moon phase"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${moonPhaseEmoji(currentMoonPhase.type)} ${moonPhaseTitle(currentMoonPhase.type)}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = tr("Освещенность", "Illumination") + ": ${currentMoonPhase.illumination}%",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { onNavigate(Screen.MOON_PHASE) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(tr("Открыть календарь", "Open calendar"))
                    }
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(tr("Мессье", "Messier")) { onNavigate(Screen.MESSIER_SEARCH) }
                QuickActionCard(tr("Наблюдения", "Observations")) { onNavigate(Screen.OBSERVATIONS) }
                QuickActionCard(tr("Фото", "Photos")) { onNavigate(Screen.PHOTOS) }
                QuickActionCard(tr("Настройки", "Settings")) { onNavigate(Screen.SETTINGS) }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(18.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SideMenuContent(
    currentScreen: Screen,
    onMenuItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val items = listOf(
                Screen.HOME to tr("Главная", "Home"),
                Screen.MOON_PHASE to tr("Фазы Луны", "Moon phases"),
                Screen.TELESCOPE_GUIDE to tr("Памятка по телескопам", "Telescope guide"),
                Screen.MESSIER_SEARCH to tr("Поиск Мессье", "Messier search"),
                Screen.PLANETS to tr("Планеты", "Planets"),
                Screen.OBSERVATIONS to tr("Наблюдения", "Observations"),
                Screen.PHOTOS to tr("Фотографии", "Photos"),
                Screen.VIDEOS to tr("Видео", "Videos"),
                Screen.SOCIAL to tr("Соцсети", "Social"),
                Screen.SETTINGS to tr("Настройки", "Settings")
            )

            items.forEachIndexed { index, (screen, title) ->
                MenuButtonContent(
                    text = title,
                    isSelected = currentScreen == screen,
                    onClick = { onMenuItemClick(screen) },
                    modifier = Modifier.padding(top = if (index == 1) 12.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuButtonContent(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}
