package com.matule.myapplication



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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.LocalMessierRepository
import com.matule.myapplication.models.MessierObject
import com.matule.myapplication.models.MoonPhaseInfo
import com.matule.myapplication.models.Planet
import com.matule.myapplication.utils.MoonPhaseCalculator
import kotlinx.coroutines.delay
import models.Screen
import utils.getCurrentDateTime
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AstronomyGuideApp() {
    var isMenuOpen by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedPlanet by remember { mutableStateOf<Planet?>(null) }
    var selectedMessierObject by remember { mutableStateOf<MessierObject?>(null) }

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
                else -> {
                    MainContent(
                        currentScreen = currentScreen,
                        currentDateTime = currentDateTime.value,
                        onMenuClick = { isMenuOpen = !isMenuOpen },
                        onNavigate = { screen ->
                            currentScreen = screen
                            selectedPlanet = null
                            selectedMessierObject = null
                            if (configuration.screenWidthDp <= 600) {
                                isMenuOpen = false
                            }
                        },
                        onPlanetClick = { planet ->
                            selectedPlanet = planet
                        },
                        onMessierObjectClick = { obj ->
                            selectedMessierObject = obj
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isMenuOpen && selectedPlanet == null && selectedMessierObject == null,
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
    onMessierObjectClick: (MessierObject) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Показываем верхнюю панель только не на некоторых экранах
        if (currentScreen != Screen.MOON_PHASE &&
            currentScreen != Screen.PLANETS &&
            currentScreen != Screen.MESSIER_SEARCH &&
            currentScreen != Screen.TELESCOPE_GUIDE) {
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
                onPlanetClick = onPlanetClick  // Только один раз!
            )

            Screen.TELESCOPE_GUIDE -> TelescopeGuideScreen(
                onBackClick = { onNavigate(Screen.HOME) }
            )

            Screen.MESSIER_SEARCH -> MessierSearchScreen(
                onBackClick = { onNavigate(Screen.HOME) },
                onObjectClick = onMessierObjectClick
            )

            Screen.VIDEOS -> VideosScreen()
            Screen.OBSERVATIONS -> ObservationsScreen()
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





@Composable
fun MessierObjectCard(
    obj: MessierObject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с номером и названием
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = obj.messierNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066CC),
                    modifier = Modifier.padding(end = 8.dp)
                )

                if (!obj.russianName.isNullOrBlank()) {
                    Text(
                        text = obj.russianName ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                }
            }

            // Основная информация в две колонки
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Левая колонка
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    InfoLine("Тип:", obj.objectType)
                    InfoLine("Созвездие:", obj.constellation)
                    InfoLine("Звезд.вел:", String.format("%.1f", obj.apparentMagnitude))
                    InfoLine("Расстояние:", String.format("%.0f св. лет", obj.distanceLy))
                }

                // Правая колонка
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    InfoLine("Угл.размер:", obj.angularSize)
                    InfoLine("Сезон:", obj.seasonVisibility)
                    if (!obj.ngcNumber.isNullOrBlank()) {
                        InfoLine("NGC:", obj.ngcNumber ?: "")
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )

            // Описание
            Text(
                text = obj.description,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Советы по наблюдению
            Surface(
                color = Color(0xFFE8F4FD),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 ${obj.observationTips}",
                    fontSize = 13.sp,
                    color = Color(0xFF006600),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.padding(start = 4.dp)
        )
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
            .background(Color(0xFFFAFAFA))
    ) {
        // Верхняя панель
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
                    text = obj.messierNumber,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Основная информация
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
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Название
                        if (!obj.russianName.isNullOrBlank()) {
                            Text(
                                text = obj.russianName ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        if (!obj.name.isNullOrBlank()) {
                            Text(
                                text = obj.name ?: "",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Характеристики
                        DetailInfoRow("Тип:", obj.objectType)
                        DetailInfoRow("Созвездие:", obj.constellation)
                        DetailInfoRow("Звёздная величина:", String.format("%.1f", obj.apparentMagnitude))
                        DetailInfoRow("Расстояние:", String.format("%.0f св. лет", obj.distanceLy))
                        DetailInfoRow("Угловой размер:", obj.angularSize)
                        DetailInfoRow("Сезон видимости:", obj.seasonVisibility)

                        if (!obj.ngcNumber.isNullOrBlank()) {
                            DetailInfoRow("NGC:", obj.ngcNumber ?: "")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Описание",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = obj.description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "🔭 Рекомендации по наблюдению",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0066CC),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = obj.observationTips,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    label: String,
    value: String
) {
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








// ============= НОВЫЕ ФУНКЦИИ ДЛЯ ФАЗ ЛУНЫ =============

@Composable
fun MoonPhaseScreen(
    onBackClick: () -> Unit
) {
    val currentDate = remember { Date() }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var moonPhaseInfo by remember { mutableStateOf(MoonPhaseCalculator.getMoonPhase(currentDate)) }
    var futurePhases by remember { mutableStateOf(MoonPhaseCalculator.getMoonPhasesForDays(currentDate, 30)) }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Верхняя панель с кнопкой назад
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
                    text = "Фазы Луны",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Текущая фаза
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Эмодзи фазы
                        Text(
                            text = moonPhaseInfo.type.emoji,
                            fontSize = 80.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Название фазы
                        Text(
                            text = moonPhaseInfo.type.displayName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        // Дата
                        Text(
                            text = dateFormat.format(selectedDate),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(vertical = 16.dp),
                            color = Color.LightGray,
                            thickness = 1.dp
                        )

                        // Информация об освещенности
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoChip(
                                icon = Icons.Default.Info,
                                label = "Освещено",
                                value = "${moonPhaseInfo.illumination}%"
                            )
                            InfoChip(
                                icon = Icons.Default.CalendarToday,
                                label = "Возраст",
                                value = String.format("%.1f дн", moonPhaseInfo.age)
                            )
                        }

                        // Описание
                        Text(
                            text = moonPhaseInfo.description,
                            fontSize = 18.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            // Заголовок для календаря фаз
            item {
                Text(
                    text = "Ближайшие фазы",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Список ближайших фаз
            items(futurePhases) { phase ->
                FutureMoonPhaseCard(phaseInfo = phase)
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.padding(start = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun FutureMoonPhaseCard(
    phaseInfo: MoonPhaseInfo
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale("ru"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Эмодзи
            Text(
                text = phaseInfo.type.emoji,
                fontSize = 32.sp,
                modifier = Modifier.width(48.dp)
            )

            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = phaseInfo.type.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = dateFormat.format(phaseInfo.date),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Освещенность
            Text(
                text = "${phaseInfo.illumination}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

// ============= ФУНКЦИИ-ЗАГЛУШКИ =============

@Composable
fun PlanetsScreen(
    onBackClick: () -> Unit,
    onPlanetClick: (Planet) -> Unit
) {
    val planets = remember { LocalPlanetDatabase.getAllPlanets() }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ru")) }
    val currentDate = remember { dateFormat.format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Верхняя панель
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
                    text = "Планеты сегодня",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Заголовок с датой
        Surface(
            color = Color(0xFFE0E0E0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🪐 Планеты сегодня ($currentDate)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Список планет
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(planets) { planet ->
                PlanetCard(
                    planet = planet,
                    onClick = { onPlanetClick(planet) }
                )
            }
        }
    }
}

@Composable
fun PlanetCard(
    planet: Planet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Название планеты
            Text(
                text = planet.russianName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Характеристики в виде сетки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlanetProperty(
                    icon = Icons.Default.Place,
                    label = "Расстояние",
                    value = String.format("%.2f а.е.", planet.distanceFromSunAu)
                )

                PlanetProperty(
                    icon = Icons.Default.Star,
                    label = "Диаметр",
                    value = String.format("%.0f км", planet.diameterKm)
                )

                PlanetProperty(
                    icon = Icons.Default.Info,
                    label = "Спутники",
                    value = planet.moonsCount.toString()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Рекомендации по наблюдению
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔭 ${planet.observationTips}",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Индикатор для подсказки о нажатии
            Text(
                text = "Нажмите для подробной информации",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
            )
        }
    }
}

@Composable
fun PlanetProperty(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}
@Composable
fun PlanetDetailScreen(
    planet: Planet,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Верхняя панель
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
                    text = planet.russianName,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Основная информация
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
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Латинское название
                        Text(
                            text = planet.latinName,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Характеристики
                        DetailRow("Расстояние от Солнца:", "${planet.distanceFromSunAu} а.е.")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        DetailRow("Диаметр:", String.format("%.0f км", planet.diameterKm))
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        DetailRow("Количество спутников:", planet.moonsCount.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Описание
                        Text(
                            text = "Описание",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Text(
                            text = planet.description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Рекомендации по наблюдению
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "🔭 Рекомендации по наблюдению",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0066CC),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = planet.observationTips,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}
@Composable
fun TelescopeGuideScreen(
    onBackClick: () -> Unit
) {
    var apertureInput by remember { mutableStateOf("70") }
    var aperture by remember { mutableStateOf(70) }
    var telescopeInfo by remember { mutableStateOf(getTelescopeInfo(70)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
    ) {
        // Верхняя панель
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
                    text = "Памятка по телескопам",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Памятка для начинающих астрономов",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Интерактивный блок для ввода апертуры
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Введите апертуру вашего телескопа (мм):",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = apertureInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                    apertureInput = newValue
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066CC),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                val parsedAperture = apertureInput.toIntOrNull()
                                if (parsedAperture != null && parsedAperture > 0) {
                                    aperture = parsedAperture
                                    telescopeInfo = getTelescopeInfo(parsedAperture)
                                } else {
                                    telescopeInfo = "Пожалуйста, введите корректное число (например: 70, 100, 150)"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            )
                        ) {
                            Text("Показать")
                        }
                    }
                }
            }

            // Результат для введенной апертуры
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF))
            ) {
                Text(
                    text = telescopeInfo,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Пошаговая инструкция
            Text(
                text = "📡 Пошаговая инструкция:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Шаги инструкции
            val steps = listOf(
                "1. Распаковка и проверка комплектации",
                "   • Труба телескопа, монтировка, штатив, окуляры (10 мм и 25 мм), искатель.",
                "   • Проверьте наличие всех деталей по инструкции.",

                "2. Сборка штатива и монтировки",
                "   • Разложите штатив, отрегулируйте высоту ног.",
                "   • Закрепите монтировку, проверьте плавность движений осей.",

                "3. Установка оптической трубы",
                "   • Закрепите трубу на монтировке, не перетягивая винты.",
                "   • Установите окуляр 25 мм (для старта).",

                "4. Настройка искателя",
                "   • Днём наведитесь на удалённый объект (антенна, дерево).",
                "   • Совместите перекрестие искателя с центром окуляра.",

                "5. Первые наблюдения",
                "   • Начните с Луны (окуляр 25 мм → 10 мм для деталей).",
                "   • Планеты: ищите Юпитер, Сатурн, Венеру.",

                "6. Фокусировка и уход",
                "   • Плавно крутите фокусер до резкости.",
                "   • После наблюдений закрывайте трубу крышкой.",
                "   • Раз в год очищайте линзы специальной салфеткой."
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    steps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 14.sp,
                            color = if (step.startsWith("   ")) Color(0xFF666666) else Color(0xFF333333),
                            fontWeight = if (!step.startsWith("   ")) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getTelescopeInfo(aperture: Int): String {
    return when {
        aperture < 60 -> {
            """Телескоп с апертурой $aperture мм подходит для:
• Наблюдения Луны: видны крупные кратеры
• Ярких планет: Венера, Юпитер (4 спутника), Сатурн (кольца)
• Ярких двойных звёзд

Рекомендации:
• Используйте окуляры с большим фокусным расстоянием (20-25 мм)
• Избегайте наблюдений в городе с сильной засветкой"""
        }
        aperture <= 80 -> {
            """Телескоп $aperture мм позволяет наблюдать:
• Луну: кратеры от 7-10 км
• Юпитер: 4 спутника, полосы на диске
• Сатурн: кольца, щель Кассини (при хороших условиях)
• Яркие туманности (Орион, Андромеда)
• Двойные звёзды с расстоянием >2"

Рекомендации:
• Для планет используйте увеличение 100-150x
• Для туманностей - широкоугольные окуляры"""
        }
        aperture <= 150 -> {
            """Телескоп $aperture мм показывает:
• Детали на Луне (борозды, мелкие кратеры)
• Щель Кассини в кольцах Сатурна
• Полярные шапки на Марсе в период противостояния
• Большинство объектов каталога Мессье
• Звёзды до 12-й величины

Рекомендации:
• Используйте фильтры для Луны и планет
• Для глубокого космоса выбирайте тёмное небо"""
        }
        aperture <= 2000 -> {
            """Телескоп $aperture мм позволяет увидеть:
Ого! Ваш телескоп похож на Хаббл!
• Кратеры на Луне <1.8 км
• Пылевые бури на Марсе
• Деление Энке в кольцах Сатурна (при идеальных условиях)
• Сотни галактик и туманностей
• Детали спиральных рукавов ярких галактик

Рекомендации:
• Требуется устойчивая монтировка
• Необходима термостабилизация телескопа
• Лучшие результаты за городом"""
        }
        else -> {
            """Телескоп $aperture мм позволяет увидеть:
Восхищаюсь вашим телескопом! Только тайно... 🌟"""
        }
    }
}

@Composable
fun MessierSearchScreen(
    onBackClick: () -> Unit,
    onObjectClick: (MessierObject) -> Unit
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<MessierObject>>(emptyList()) }

    // Инициализируем репозиторий
    val repository = remember { LocalMessierRepository(context) }

    // Загружаем все объекты при первом запуске
    LaunchedEffect(Unit) {
        searchResults = repository.searchObjects("")
    }

    // Функция поиска
    fun performSearch(query: String) {
        searchResults = repository.searchObjects(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Верхняя панель
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
                    text = "Поиск объектов Мессье",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = "🔍 Поиск объектов Мессье",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Поле поиска
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    performSearch(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите M1, M42, Туманность...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Результаты
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Объекты не найдены",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Text(
                    text = "Найдено объектов: ${searchResults.size}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = searchResults,
                        key = { it.id }
                    ) { obj ->
                        MessierObjectCard(
                            obj = obj,
                            onClick = { onObjectClick(obj) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PlanetsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Планеты - в разработке")
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
data class Planet(
    val id: Int,
    val russianName: String,
    val latinName: String,
    val distanceFromSunAu: Double,
    val diameterKm: Double,
    val moonsCount: Int,
    val observationTips: String,
    val description: String
)

// Добавьте это в самый конец файла MainActivity.kt, перед последней }

// Временный репозиторий (пока не заработает импорт)
object TempMessierRepository {

    private val messierObjects = listOf(
        MessierObject(
            id = 1,
            messierNumber = "M1",
            ngcNumber = "NGC 1952",
            name = "Crab Nebula",
            russianName = "Крабовидная туманность",
            objectType = "Остаток сверхновой",
            constellation = "Телец",
            apparentMagnitude = 8.4,
            distanceLy = 6500.0,
            angularSize = "6' × 4'",
            seasonVisibility = "Зима",
            description = "Остаток взрыва сверхновой, зарегистрированного в 1054 году.",
            observationTips = "Виден в небольшой телескоп при хороших условиях."
        ),
        MessierObject(
            id = 2,
            messierNumber = "M13",
            ngcNumber = "NGC 6205",
            name = "Great Hercules Cluster",
            russianName = "Великое скопление в Геркулесе",
            objectType = "Шаровое скопление",
            constellation = "Геркулес",
            apparentMagnitude = 5.8,
            distanceLy = 25100.0,
            angularSize = "20'",
            seasonVisibility = "Лето",
            description = "Одно из самых впечатляющих шаровых скоплений северного неба.",
            observationTips = "Видно невооружённым глазом в идеальных условиях."
        ),
        MessierObject(
            id = 3,
            messierNumber = "M31",
            ngcNumber = "NGC 224",
            name = "Andromeda Galaxy",
            russianName = "Туманность Андромеды",
            objectType = "Галактика",
            constellation = "Андромеда",
            apparentMagnitude = 3.4,
            distanceLy = 2540000.0,
            angularSize = "190' × 60'",
            seasonVisibility = "Осень",
            description = "Ближайшая к нам крупная галактика.",
            observationTips = "Лучшее время наблюдения - осень."
        ),
        MessierObject(
            id = 4,
            messierNumber = "M42",
            ngcNumber = "NGC 1976",
            name = "Orion Nebula",
            russianName = "Туманность Ориона",
            objectType = "Эмиссионная туманность",
            constellation = "Орион",
            apparentMagnitude = 4.0,
            distanceLy = 1344.0,
            angularSize = "85' × 60'",
            seasonVisibility = "Зима",
            description = "Ярчайшая туманность земного неба.",
            observationTips = "Видна невооружённым глазом как звезда в мече Ориона."
        ),
        MessierObject(
            id = 5,
            messierNumber = "M45",
            ngcNumber = null,
            name = "Pleiades",
            russianName = "Плеяды",
            objectType = "Рассеянное скопление",
            constellation = "Телец",
            apparentMagnitude = 1.6,
            distanceLy = 444.0,
            angularSize = "110'",
            seasonVisibility = "Зима",
            description = "Одно из ближайших и самых красивых рассеянных скоплений.",
            observationTips = "Прекрасно видно невооружённым глазом."
        )
    )

    fun searchObjects(searchTerm: String): List<MessierObject> {
        if (searchTerm.isBlank()) return emptyList()

        val term = searchTerm.lowercase()

        return messierObjects.filter { obj ->
            obj.messierNumber.lowercase().contains(term) ||
                    obj.russianName?.lowercase()?.contains(term) == true ||
                    obj.name?.lowercase()?.contains(term) == true ||
                    obj.ngcNumber?.lowercase()?.contains(term) == true ||
                    obj.objectType.lowercase().contains(term) ||
                    obj.constellation.lowercase().contains(term)
        }.sortedBy { it.messierNumber }
    }
}
// База данных планет
object LocalPlanetDatabase {
    fun getAllPlanets(): List<Planet> {
        return listOf(
            Planet(1, "Меркурий", "Mercury", 0.39, 4879.4, 0,
                "Виден низко над горизонтом", "Самая близкая к Солнцу планета."),
            Planet(2, "Венера", "Venus", 0.72, 12104.0, 0,
                "Очень яркая, видна вечером", "Самая горячая планета."),
            Planet(3, "Земля", "Earth", 1.0, 12742.0, 1,
                "Наш дом", "Единственная планета с жизнью."),
            Planet(4, "Марс", "Mars", 1.52, 6779.0, 2,
                "Красноватый оттенок", "Красная планета."),
            Planet(5, "Юпитер", "Jupiter", 5.2, 139820.0, 79,
                "Видны 4 спутника", "Самая большая планета."),
            Planet(6, "Сатурн", "Saturn", 9.58, 116460.0, 82,
                "Видны кольца", "Планета с кольцами."),
            Planet(7, "Уран", "Uranus", 19.2, 50724.0, 27,
                "Зеленоватый диск", "Вращается на боку."),
            Planet(8, "Нептун", "Neptune", 30.05, 49244.0, 14,
                "Очень тусклый", "Самая ветреная планета.")
        )
    }
}
@Composable
fun ObservationsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Мои наблюдения - в разработке")
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
