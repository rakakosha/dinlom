package com.matule.myapplication.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.matule.myapplication.data.LocalMessierRepository
import com.matule.myapplication.data.ObservationRepository
import com.matule.myapplication.data.SettingsRepository
import com.matule.myapplication.models.AppSettings
import com.matule.myapplication.models.MessierObject
import com.matule.myapplication.models.Planet
import com.matule.myapplication.models.Screen
import com.matule.myapplication.models.UserObservation

val LocalObservationRepository = staticCompositionLocalOf<ObservationRepository> {
    error("ObservationRepository is not provided")
}

val LocalMessierDataRepository = staticCompositionLocalOf<LocalMessierRepository> {
    error("LocalMessierRepository is not provided")
}

val LocalSettingsRepository = staticCompositionLocalOf<SettingsRepository> {
    error("SettingsRepository is not provided")
}

@Stable
class AstronomyGuideAppState(
    initialSettings: AppSettings,
    val settingsRepository: SettingsRepository,
    val observationRepository: ObservationRepository,
    val messierRepository: LocalMessierRepository
) {
    var settings by mutableStateOf(initialSettings)
        private set

    var isMenuOpen by mutableStateOf(false)
        private set

    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    var selectedPlanet by mutableStateOf<Planet?>(null)
        private set

    var selectedMessierObject by mutableStateOf<MessierObject?>(null)
        private set

    var selectedObservation by mutableStateOf<UserObservation?>(null)
        private set

    var editorObservation by mutableStateOf<UserObservation?>(null)
        private set

    var isEditingObservation by mutableStateOf(false)
        private set

    var currentDateTime by mutableStateOf(getCurrentDateTime())
        private set

    val isSecondaryContentVisible: Boolean
        get() = selectedPlanet != null ||
            selectedMessierObject != null ||
            selectedObservation != null ||
            isEditingObservation

    fun toggleMenu() {
        isMenuOpen = !isMenuOpen
    }

    fun updateCurrentDateTime() {
        currentDateTime = getCurrentDateTime()
    }

    fun navigateTo(screen: Screen, closeMenu: Boolean = false) {
        currentScreen = screen
        selectedPlanet = null
        selectedMessierObject = null
        selectedObservation = null
        editorObservation = null
        isEditingObservation = false
        if (closeMenu) {
            isMenuOpen = false
        }
    }

    fun openPlanet(planet: Planet) {
        selectedPlanet = planet
    }

    fun closePlanetDetails() {
        selectedPlanet = null
        currentScreen = Screen.PLANETS
    }

    fun openMessierObject(messierObject: MessierObject) {
        selectedMessierObject = messierObject
    }

    fun closeMessierObjectDetails() {
        selectedMessierObject = null
        currentScreen = Screen.MESSIER_SEARCH
    }

    fun openObservation(observation: UserObservation) {
        selectedObservation = observation
    }

    fun dismissObservationDetails() {
        selectedObservation = null
    }

    fun startObservationEditor(observation: UserObservation? = null) {
        editorObservation = observation
        selectedObservation = null
        isEditingObservation = true
    }

    fun stopEditingObservation() {
        editorObservation = null
        isEditingObservation = false
    }

    fun saveObservation(observation: UserObservation) {
        observationRepository.saveObservation(observation)
        stopEditingObservation()
        currentScreen = Screen.OBSERVATIONS
    }

    fun deleteSelectedObservation() {
        selectedObservation?.let { observationRepository.deleteObservation(it.id) }
        selectedObservation = null
        currentScreen = Screen.OBSERVATIONS
    }

    fun applySettings(newSettings: AppSettings, goHome: Boolean) {
        settings = newSettings
        settingsRepository.saveSettings(newSettings)
        if (goHome) {
            navigateTo(Screen.HOME, closeMenu = true)
        }
    }
}

@Composable
fun rememberAstronomyGuideAppState(): AstronomyGuideAppState {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val observationRepository = remember { ObservationRepository(context) }
    val messierRepository = remember { LocalMessierRepository(context) }

    return remember(settingsRepository, observationRepository, messierRepository) {
        AstronomyGuideAppState(
            initialSettings = settingsRepository.loadSettings(),
            settingsRepository = settingsRepository,
            observationRepository = observationRepository,
            messierRepository = messierRepository
        )
    }
}
