package com.matule.myapplication.utils

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.models.AppSettings
import com.matule.myapplication.models.MessierObject
import com.matule.myapplication.models.Planet
import com.matule.myapplication.models.Screen
import com.matule.myapplication.models.UserObservation
import kotlinx.coroutines.delay
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstronomyGuideApp()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AstronomyGuideApp() {
    val configuration = LocalConfiguration.current
    val activity = LocalContext.current as? Activity
    val density = LocalDensity.current
    val appState = rememberAstronomyGuideAppState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            appState.updateCurrentDateTime()
        }
    }

    val colorScheme = if (appState.settings.theme == "dark") {
        darkColorScheme(
            primary = Color(0xFF90CAF9),
            secondary = Color(0xFF80CBC4),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2A2A2A)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1E4D8C),
            secondary = Color(0xFF2F7D6C),
            background = Color(0xFFF5F7FA),
            surface = Color.White,
            surfaceVariant = Color(0xFFE8EEF5)
        )
    }

    CompositionLocalProvider(
        LocalAppSettings provides appState.settings,
        LocalSettingsRepository provides appState.settingsRepository,
        LocalObservationRepository provides appState.observationRepository,
        LocalMessierDataRepository provides appState.messierRepository,
        LocalAstroCalculator provides appState.astroCalculator,
        LocalDensity provides Density(density.density, appState.settings.fontScale)
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (appState.isMenuOpen && configuration.screenWidthDp > 600) {
                                Modifier.fillMaxWidth(fraction = 0.7f)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    when {
                        appState.selectedPlanet != null -> {
                            PlanetDetailScreenContent(
                                planet = appState.selectedPlanet!!,
                                onBackClick = appState::closePlanetDetails
                            )
                        }

                        appState.selectedMessierObject != null -> {
                            MessierObjectDetailScreen(
                                obj = appState.selectedMessierObject!!,
                                onBackClick = appState::closeMessierObjectDetails
                            )
                        }

                        appState.selectedObservation != null -> {
                            ObservationDetailScreenContent(
                                observation = appState.selectedObservation!!,
                                onBackClick = appState::dismissObservationDetails,
                                onEditClick = {
                                    appState.startObservationEditor(appState.selectedObservation)
                                },
                                onDeleteClick = appState::deleteSelectedObservation
                            )
                        }

                        appState.isEditingObservation -> {
                            ObservationEditorScreenContent(
                                initialObservation = appState.editorObservation,
                                onBackClick = appState::stopEditingObservation,
                                onSaveClick = appState::saveObservation
                            )
                        }

                        else -> {
                            MainContent(
                                currentScreen = appState.currentScreen,
                                currentDateTime = appState.currentDateTime,
                                currentSettings = appState.settings,
                                onMenuClick = appState::toggleMenu,
                                onNavigate = { screen ->
                                    appState.navigateTo(screen, closeMenu = configuration.screenWidthDp <= 600)
                                },
                                onPlanetClick = appState::openPlanet,
                                onMessierObjectClick = appState::openMessierObject,
                                onObservationClick = appState::openObservation,
                                onAddObservationClick = { appState.startObservationEditor() },
                                onEditObservationClick = appState::startObservationEditor,
                                onApplySettings = appState::applySettings
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = appState.isMenuOpen && !appState.isSecondaryContentVisible,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    SideMenuContent(
                        currentScreen = appState.currentScreen,
                        onMenuItemClick = { screen ->
                            appState.navigateTo(screen, closeMenu = configuration.screenWidthDp <= 600)
                        },
                        onCloseAppClick = { activity?.finishAffinity() },
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(if (configuration.screenWidthDp > 600) 260.dp else 220.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent(
    currentScreen: Screen,
    currentDateTime: String,
    currentSettings: AppSettings,
    onMenuClick: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onPlanetClick: (Planet) -> Unit,
    onMessierObjectClick: (MessierObject) -> Unit,
    onObservationClick: (UserObservation) -> Unit,
    onAddObservationClick: () -> Unit,
    onEditObservationClick: (UserObservation) -> Unit,
    onApplySettings: (AppSettings, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (currentScreen == Screen.HOME) {
            TopBarContent(
                currentDateTime = currentDateTime,
                onMenuClick = onMenuClick
            )
        }

        when (currentScreen) {
            Screen.HOME -> HomeScreenContent(onNavigate = onNavigate)
            Screen.ASTRO_NEWS -> AstroNewsScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.MOON_PHASE -> MoonPhaseScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.TELESCOPE_GUIDE -> TelescopeGuideScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.MESSIER_SEARCH -> MessierSearchScreen(
                onBackClick = { onNavigate(Screen.HOME) },
                onObjectClick = onMessierObjectClick
            )
            Screen.PLANETS -> PlanetsScreenContent(
                onBackClick = { onNavigate(Screen.HOME) },
                onPlanetClick = onPlanetClick
            )
            Screen.VIDEOS -> VideosScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.OBSERVATIONS -> ObservationsScreenContent(
                onBackClick = { onNavigate(Screen.HOME) },
                onObservationClick = onObservationClick,
                onAddObservationClick = onAddObservationClick,
                onEditObservationClick = onEditObservationClick
            )
            Screen.PHOTOS -> PhotosScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.SOCIAL -> SocialScreenContent(onBackClick = { onNavigate(Screen.HOME) })
            Screen.SETTINGS -> SettingsScreenContent(
                currentSettings = currentSettings,
                onBackClick = { onNavigate(Screen.HOME) },
                onApplySettings = onApplySettings
            )
        }
    }
}

@Composable
fun MessierSearchScreen(
    onBackClick: () -> Unit,
    onObjectClick: (MessierObject) -> Unit
) {
    val repository = LocalMessierDataRepository.current
    var searchText by remember { mutableStateOf("") }
    val results = remember(searchText) { repository.searchObjects(searchText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Объекты Мессье", "Messier objects"),
            onBackClick = onBackClick
        )

        androidx.compose.material3.OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text(tr("Поиск: M42, Андромеда, Orion...", "Search: M42, Andromeda, Orion...")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items(results) { obj ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onObjectClick(obj) },
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${obj.messierNumber} ${obj.russianName ?: obj.name ?: ""}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${obj.objectType}, ${obj.constellation}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessierObjectDetailScreen(
    obj: MessierObject,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = obj.messierNumber,
            onBackClick = onBackClick
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(obj.russianName ?: obj.name ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(tr("Тип", "Type") + ": ${obj.objectType}", modifier = Modifier.padding(top = 12.dp))
            Text(tr("Созвездие", "Constellation") + ": ${obj.constellation}")
            Text(tr("Яркость", "Magnitude") + ": ${obj.apparentMagnitude}")
            Text(tr("Расстояние", "Distance") + ": ${obj.distanceLy} ${tr("св. лет", "ly")}")
            Text(tr("Размер", "Angular size") + ": ${obj.angularSize}")

            Text(tr("Описание", "Description"), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Text(obj.description, modifier = Modifier.padding(top = 6.dp))

            Text(tr("Советы по наблюдению", "Observation tips"), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Text(obj.observationTips, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(tr("Выберите дату", "Choose a date")) },
        text = {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(tr("Отмена", "Cancel"))
            }
        }
    )
}
