package com.matule.myapplication.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.ObservationRepository
import com.matule.myapplication.models.MessierObject
import com.matule.myapplication.models.Planet
import com.matule.myapplication.models.Screen
import com.matule.myapplication.models.UserObservation

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AstronomyGuideApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AstronomyGuideApp() {
    var isMenuOpen by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedPlanet by remember { mutableStateOf<Planet?>(null) }
    var selectedMessierObject by remember { mutableStateOf<MessierObject?>(null) }
    var selectedObservation by remember { mutableStateOf<UserObservation?>(null) }
    var isAddingObservation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val currentDateTime = remember {
        mutableStateOf(getCurrentDateTime())
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentDateTime.value = getCurrentDateTime()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Основной контент
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isMenuOpen) {
                        if (configuration.screenWidthDp > 600) {
                            Modifier.fillMaxWidth(fraction = 0.7f)
                        } else Modifier
                    } else Modifier
                )
        ) {
            when {
                selectedPlanet != null -> {
                    PlanetDetailScreen(
                        planet = selectedPlanet!!,
                        onBackClick = {
                            selectedPlanet = null
                            currentScreen = Screen.PLANETS
                        }
                    )
                }
                selectedMessierObject != null -> {
                    MessierObjectDetailScreen(
                        obj = selectedMessierObject!!,
                        onBackClick = {
                            selectedMessierObject = null
                            currentScreen = Screen.MESSIER_SEARCH
                        }
                    )
                }
                selectedObservation != null -> {
                    ObservationDetailScreen(
                        observation = selectedObservation!!,
                        onBackClick = { selectedObservation = null },
                        onEditClick = {
                            selectedObservation = null
                            isAddingObservation = true
                        },
                        onDeleteClick = {
                            val repo = ObservationRepository(context)
                            repo.deleteObservation(selectedObservation!!.id)
                            selectedObservation = null
                        }
                    )
                }
                isAddingObservation -> {
                    AddObservationScreen(
                        onBackClick = { isAddingObservation = false },
                        onSaveClick = { observation ->
                            val repo = ObservationRepository(context)
                            repo.saveObservation(observation)
                            isAddingObservation = false
                        }
                    )
                }
                else -> {
                    MainContent(
                        currentScreen = currentScreen,
                        currentDateTime = currentDateTime.value,
                        onMenuClick = { isMenuOpen = !isMenuOpen },
                        onNavigate = { screen ->
                            currentScreen = screen
                            selectedPlanet = null
                            selectedMessierObject = null
                            selectedObservation = null
                            isAddingObservation = false
                            if (configuration.screenWidthDp <= 600) {
                                isMenuOpen = false
                            }
                        },
                        onPlanetClick = { planet ->
                            selectedPlanet = planet
                        },
                        onMessierObjectClick = { obj ->
                            selectedMessierObject = obj
                        },
                        onObservationClick = { observation ->
                            selectedObservation = observation
                        },
                        onAddObservationClick = {
                            isAddingObservation = true
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isMenuOpen && selectedPlanet == null &&
                    selectedMessierObject == null &&
                    selectedObservation == null &&
                    !isAddingObservation,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            SideMenu(
                currentScreen = currentScreen,
                onMenuItemClick = { screen ->
                    currentScreen = screen
                    selectedPlanet = null
                    selectedMessierObject = null
                    selectedObservation = null
                    isAddingObservation = false
                    if (configuration.screenWidthDp <= 600) {
                        isMenuOpen = false
                    }
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (configuration.screenWidthDp > 600) 260.dp else 220.dp)
            )
        }
    }
}

@Composable
fun MainContent(
    currentScreen: Screen,
    currentDateTime: String,
    onMenuClick: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onPlanetClick: (Planet) -> Unit,
    onMessierObjectClick: (MessierObject) -> Unit,
    onObservationClick: (UserObservation) -> Unit,
    onAddObservationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        if (currentScreen != Screen.MOON_PHASE &&
            currentScreen != Screen.PLANETS &&
            currentScreen != Screen.MESSIER_SEARCH &&
            currentScreen != Screen.TELESCOPE_GUIDE &&
            currentScreen != Screen.OBSERVATIONS) {
            TopBar(
                currentDateTime = currentDateTime,
                onMenuClick = onMenuClick
            )
        }

        when (currentScreen) {
            Screen.HOME -> HomeScreen(
                onNavigate = onNavigate
            )

            Screen.MOON_PHASE -> MoonPhaseScreen(
                onBackClick = { onNavigate(Screen.HOME) }
            )

            Screen.PLANETS -> PlanetsScreen(
                onBackClick = { onNavigate(Screen.HOME) },
                onPlanetClick = onPlanetClick
            )

            Screen.TELESCOPE_GUIDE -> TelescopeGuideScreen(
                onBackClick = { onNavigate(Screen.HOME) }
            )

            Screen.MESSIER_SEARCH -> MessierSearchScreen(
                onBackClick = { onNavigate(Screen.HOME) },
                onObjectClick = onMessierObjectClick
            )

            Screen.OBSERVATIONS -> ObservationsScreen(
                onBackClick = { onNavigate(Screen.HOME) },
                onObservationClick = onObservationClick,
                onAddObservationClick = onAddObservationClick
            )

            Screen.VIDEOS -> VideosScreen()
            Screen.SOCIAL -> SocialScreen()
            Screen.SETTINGS -> SettingsScreen()
        }
    }
}

@Composable
fun TopBar(
    currentDateTime: String,
    onMenuClick: () -> Unit
) {
    Surface(
        color = Color(0xFF333333),
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
                    text = "Астрономический гид",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = currentDateTime,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    val currentMoonPhase = remember { MoonPhaseCalculator.getMoonPhase(Date()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = "Добро пожаловать в Астрономический гид!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Здесь вы найдёте последние астрономические новости, информацию о видимости планет, полезные советы по наблюдениям и многое другое.",
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { onNavigate(Screen.PLANETS) },
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .wrapContentWidth()
                    ) {
                        Text("Начать знакомство")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Text(
                            text = "Сегодня на небе",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        Text(
                            text = "Венера (яркая), Марс, Юпитер. МКС пролетит в 20:15",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Button(
                            onClick = { onNavigate(Screen.PLANETS) },
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text("Подробнее")
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentMoonPhase.type.emoji,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Фаза Луны",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${currentMoonPhase.type.displayName}, ${currentMoonPhase.illumination}%",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
                        )
                        Button(
                            onClick = { onNavigate(Screen.MOON_PHASE) },
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text("Подробнее")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SideMenu(
    currentScreen: Screen,
    onMenuItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                MenuButton(
                    text = "Главная",
                    isSelected = currentScreen == Screen.HOME,
                    onClick = { onMenuItemClick(Screen.HOME) }
                )

                MenuButton(
                    text = "Фазы Луны",
                    isSelected = currentScreen == Screen.MOON_PHASE,
                    onClick = { onMenuItemClick(Screen.MOON_PHASE) },
                    modifier = Modifier.padding(top = 20.dp)
                )

                MenuButton(
                    text = "Памятка по телескопам",
                    isSelected = currentScreen == Screen.TELESCOPE_GUIDE,
                    onClick = { onMenuItemClick(Screen.TELESCOPE_GUIDE) }
                )

                MenuButton(
                    text = "Поиск Мессье",
                    isSelected = currentScreen == Screen.MESSIER_SEARCH,
                    onClick = { onMenuItemClick(Screen.MESSIER_SEARCH) }
                )

                MenuButton(
                    text = "Планеты",
                    isSelected = currentScreen == Screen.PLANETS,
                    onClick = { onMenuItemClick(Screen.PLANETS) }
                )

                MenuButton(
                    text = "Видео о звёздах",
                    isSelected = currentScreen == Screen.VIDEOS,
                    onClick = { onMenuItemClick(Screen.VIDEOS) }
                )

                MenuButton(
                    text = "Мои наблюдения",
                    isSelected = currentScreen == Screen.OBSERVATIONS,
                    onClick = { onMenuItemClick(Screen.OBSERVATIONS) }
                )

                MenuButton(
                    text = "Соцсети",
                    isSelected = currentScreen == Screen.SOCIAL,
                    onClick = { onMenuItemClick(Screen.SOCIAL) }
                )

                MenuButton(
                    text = "Настройки",
                    isSelected = currentScreen == Screen.SETTINGS,
                    onClick = { onMenuItemClick(Screen.SETTINGS) }
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredState by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHoveredState) {
        isHovered = isHoveredState
    }

    Surface(
        color = when {
            isSelected -> Color(0xFF3A3A3A)
            isHovered -> Color(0xFF3A3A3A)
            else -> Color.Transparent
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(
                    scaleX = if (isHovered) 1.05f else 1f,
                    scaleY = if (isHovered) 1.05f else 1f
                )
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// ============= ЭКРАНЫ НАБЛЮДЕНИЙ =============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreen(
    onBackClick: () -> Unit,
    onObservationClick: (UserObservation) -> Unit,
    onAddObservationClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ObservationRepository(context) }
    var observations by remember { mutableStateOf(repository.getAllObservations()) }
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredObservations = when (selectedFilter) {
        "planned" -> observations.filter { it.status == "planned" }
        "completed" -> observations.filter { it.status == "completed" }
        "cancelled" -> observations.filter { it.status == "cancelled" }
        else -> observations
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Surface(
            color = Color(0xFF333333),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Мои наблюдения",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onAddObservationClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить",
                        tint = Color.White
                    )
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = when (selectedFilter) {
                "all" -> 0
                "planned" -> 1
                "completed" -> 2
                "cancelled" -> 3
                else -> 0
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            edgePadding = 8.dp
        ) {
            listOf("Все", "Запланированные", "Завершенные", "Отмененные").forEachIndexed { index, title ->
                Tab(
                    selected = when (selectedFilter) {
                        "all" -> index == 0
                        "planned" -> index == 1
                        "completed" -> index == 2
                        "cancelled" -> index == 3
                        else -> false
                    },
                    onClick = {
                        selectedFilter = when (index) {
                            0 -> "all"
                            1 -> "planned"
                            2 -> "completed"
                            3 -> "cancelled"
                            else -> "all"
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        if (filteredObservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Нет наблюдений",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Нажмите + чтобы добавить",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredObservations,
                    key = { it.id }
                ) { observation ->
                    ObservationCard(
                        observation = observation,
                        onClick = { onObservationClick(observation) },
                        repository = repository
                    )
                }
            }
        }
    }
}

@Composable
fun ObservationCard(
    observation: UserObservation,
    onClick: () -> Unit,
    repository: ObservationRepository
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val photos by remember { mutableStateOf(repository.getPhotosForObservation(observation.id)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (observation.status) {
                "planned" -> Color.White
                "completed" -> Color(0xFFE8F4FD)
                "cancelled" -> Color(0xFFFFE8E8)
                else -> Color.White
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = observation.objectName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    if (!observation.objectType.isNullOrBlank()) {
                        Text(
                            text = " (${observation.objectType})",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { /* Edit */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("✏️", fontSize = 16.sp)
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("🗑️", fontSize = 16.sp)
                    }

                    IconButton(
                        onClick = { /* Add photo */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("📷", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoLine(
                        icon = "📅",
                        text = formatDate(observation.observationDate)
                    )
                    if (!observation.location.isNullOrBlank()) {
                        InfoLine(
                            icon = "📍",
                            text = observation.location!!
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (!observation.telescopeUsed.isNullOrBlank()) {
                        InfoLine(
                            icon = "🔭",
                            text = observation.telescopeUsed!!
                        )
                    }
                    InfoLine(
                        icon = "⭐",
                        text = "Качество: ${observation.seeingRating}/5"
                    )
                }
            }

            if (photos.isNotEmpty()) {
                Text(
                    text = "📷 ${photos.size} фото",
                    fontSize = 12.sp,
                    color = Color.Blue,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { /* Show photos */ }
                )
            }

            if (!observation.personalNotes.isNullOrBlank()) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray
                )
                Text(
                    text = observation.personalNotes!!,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontStyle = FontStyle.Italic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление наблюдения") },
            text = { Text("Вы уверены, что хотите удалить это наблюдение?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteObservation(observation.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun InfoLine(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(
            text = " $text",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObservationScreen(
    onBackClick: () -> Unit,
    onSaveClick: (UserObservation) -> Unit
) {
    var objectName by remember { mutableStateOf("") }
    var objectType by remember { mutableStateOf("") }
    var observationDate by remember { mutableStateOf(Date()) }
    var location by remember { mutableStateOf("") }
    var telescopeUsed by remember { mutableStateOf("") }
    var weatherConditions by remember { mutableStateOf("") }
    var seeingRating by remember { mutableStateOf(3) }
    var personalNotes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("planned") }

    val showDatePicker = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Surface(
            color = Color(0xFF333333),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Новое наблюдение",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        if (objectName.isNotBlank()) {
                            val observation = UserObservation(
                                objectName = objectName,
                                objectType = objectType.takeIf { it.isNotBlank() },
                                observationDate = observationDate,
                                location = location.takeIf { it.isNotBlank() },
                                telescopeUsed = telescopeUsed.takeIf { it.isNotBlank() },
                                weatherConditions = weatherConditions.takeIf { it.isNotBlank() },
                                seeingRating = seeingRating,
                                personalNotes = personalNotes.takeIf { it.isNotBlank() },
                                status = status
                            )
                            onSaveClick(observation)
                        }
                    }
                ) {
                    Text("Сохранить", color = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = objectName,
                    onValueChange = { objectName = it },
                    label = { Text("Название объекта *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = objectName.isBlank()
                )
            }

            item {
                OutlinedTextField(
                    value = objectType,
                    onValueChange = { objectType = it },
                    label = { Text("Тип объекта") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatDate(observationDate),
                        onValueChange = {},
                        label = { Text("Дата наблюдения") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )

                    IconButton(
                        onClick = { showDatePicker.value = true },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Выбрать дату"
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Место наблюдения") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = telescopeUsed,
                    onValueChange = { telescopeUsed = it },
                    label = { Text("Используемый телескоп") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = weatherConditions,
                    onValueChange = { weatherConditions = it },
                    label = { Text("Погодные условия") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Оценка качества наблюдения: $seeingRating/5",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Slider(
                    value = seeingRating.toFloat(),
                    onValueChange = { seeingRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text("Заметки") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Статус наблюдения",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = status == "planned",
                                onClick = { status = "planned" },
                                label = { Text("Запланировано") }
                            )
                            FilterChip(
                                selected = status == "completed",
                                onClick = { status = "completed" },
                                label = { Text("Выполнено") }
                            )
                            FilterChip(
                                selected = status == "cancelled",
                                onClick = { status = "cancelled" },
                                label = { Text("Отменено") }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            onDateSelected = { millis ->
                millis?.let {
                    observationDate = Date(it)
                    showDatePicker.value = false
                }
            }
        )
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
        title = { Text("Выберите дату") },
        text = {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ObservationDetailScreen(
    observation: UserObservation,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ObservationRepository(context) }
    val photos by remember { mutableStateOf(repository.getPhotosForObservation(observation.id)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Surface(
            color = Color(0xFF333333),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Text(
                    text = observation.objectName,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (observation.status) {
                            "planned" -> Color.White
                            "completed" -> Color(0xFFE8F4FD)
                            "cancelled" -> Color(0xFFFFE8E8)
                            else -> Color.White
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Основная информация",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        DetailRow("Тип объекта:", observation.objectType ?: "Не указан")
                        DetailRow("Дата:", formatDate(observation.observationDate))
                        DetailRow("Место:", observation.location ?: "Не указано")
                        DetailRow("Телескоп:", observation.telescopeUsed ?: "Не указан")
                        DetailRow("Погода:", observation.weatherConditions ?: "Не указано")
                        DetailRow("Качество:", "${observation.seeingRating}/5")
                        DetailRow("Статус:", getStatusText(observation.status))
                    }
                }
            }

            if (!observation.personalNotes.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Заметки",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = observation.personalNotes!!,
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            if (photos.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Фотографии (${photos.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            photos.forEach { photo ->
                                Text(
                                    text = "📷 ${photo.fileName}",
                                    fontSize = 14.sp,
                                    color = Color.Blue,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { /* Открыть фото */ }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Удалить наблюдение")
                }
            }
        }
    }
}

fun getStatusText(status: String): String {
    return when (status) {
        "planned" -> "Запланировано"
        "completed" -> "Выполнено"
        "cancelled" -> "Отменено"
        else -> status
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.weight(2f)
        )
    }
}

// ============= ЭКРАНЫ-ЗАГЛУШКИ =============

@Composable
fun MoonPhaseScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Фазы Луны - в разработке")
    }
}

@Composable
fun PlanetsScreen(
    onBackClick: () -> Unit,
    onPlanetClick: (Planet) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Планеты - в разработке")
    }
}

@Composable
fun TelescopeGuideScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Памятка по телескопам - в разработке")
    }
}

@Composable
fun MessierSearchScreen(
    onBackClick: () -> Unit,
    onObjectClick: (MessierObject) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Поиск Мессье - в разработке")
    }
}

@Composable
fun MessierObjectDetailScreen(
    obj: MessierObject,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Детали объекта Мессье - в разработке")
    }
}

@Composable
fun PlanetDetailScreen(
    planet: Planet,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Детали планеты - в разработке")
    }
}

@Composable
fun VideosScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Видео о звёздах - в разработке")
    }
}

@Composable
fun SocialScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Соцсети - в разработке")
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Настройки - в разработке")
    }
}