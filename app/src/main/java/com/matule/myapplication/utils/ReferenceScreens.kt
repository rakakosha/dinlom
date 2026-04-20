package com.matule.myapplication.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.models.AppSettings

private data class VideoItem(
    val title: String,
    val duration: String,
    val description: String,
    val link: String
)

private data class SocialLink(
    val title: String,
    val link: String,
    val description: String
)

private val telescopeGuideSteps = listOf(
    "1. Распакуйте комплект и проверьте трубу, штатив, монтировку и окуляры.",
    "2. Установите штатив на устойчивую поверхность и выровняйте его.",
    "3. Закрепите трубу и начните с окуляра с меньшим увеличением.",
    "4. Днем настройте искатель по далекому наземному объекту.",
    "5. Первые наблюдения лучше начинать с Луны, Венеры, Юпитера или Сатурна.",
    "6. После сеанса закрывайте оптику крышками и храните её в сухом месте."
)

private fun getTelescopeInfo(aperture: Int): String {
    return when {
        aperture < 60 -> "Телескоп с апертурой $aperture мм лучше всего подходит для Луны, ярких планет и двойных звёзд."
        aperture <= 80 -> "Телескоп $aperture мм уже покажет фазы Венеры, спутники Юпитера и кольца Сатурна."
        aperture <= 150 -> "Телескоп $aperture мм хорошо подходит для большинства объектов Мессье и детальных наблюдений Луны."
        aperture <= 250 -> "Телескоп $aperture мм раскрывает тусклые туманности, шаровые скопления и детали на планетах."
        else -> "Телескоп $aperture мм уже тянет на серьёзный инструмент для глубокого неба и тонких планетных деталей."
    }
}

@Composable
fun TelescopeGuideScreenContent(onBackClick: () -> Unit) {
    var apertureText by remember { mutableStateOf("70") }
    val apertureValue = apertureText.toIntOrNull()
    val telescopeInfo = apertureValue?.takeIf { it > 0 }?.let(::getTelescopeInfo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Памятка по телескопам", "Telescope guide"),
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
                        Text(
                            tr("Введите апертуру телескопа в миллиметрах", "Enter telescope aperture in millimeters"),
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = apertureText,
                            onValueChange = { apertureText = it.filter(Char::isDigit) },
                            label = { Text(tr("Апертура", "Aperture")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                        Text(
                            telescopeInfo ?: tr(
                                "Введите корректное число, например 70, 100 или 150.",
                                "Enter a valid number like 70, 100 or 150."
                            ),
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }
                }
            }

            items(telescopeGuideSteps) { step ->
                Card(shape = RoundedCornerShape(14.dp)) {
                    Text(step, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun VideosScreenContent(onBackClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val videos = remember {
        listOf(
            VideoItem(
                title = "Строение Вселенной (NASA)",
                duration = "15:32",
                description = "Краткое и наглядное путешествие по масштабам Вселенной, галактикам и космическим структурам.",
                link = "https://www.youtube.com/@NASA"
            ),
            VideoItem(
                title = "Как рождаются звезды",
                duration = "22:15",
                description = "Видео о звездообразовании, туманностях и молодых звёздных скоплениях.",
                link = "https://www.youtube.com/@ESOobservatory"
            ),
            VideoItem(
                title = "Топ космических явлений",
                duration = "10:45",
                description = "Подборка заметных космических явлений, которые интересно наблюдать любителю.",
                link = "https://www.youtube.com/@NASAHubble"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Видео о звездах", "Space videos"), onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            tr("Длительность", "Duration") + ": ${video.duration}",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(video.description, modifier = Modifier.padding(top = 10.dp))
                        Button(
                            onClick = { uriHandler.openUri(video.link) },
                            modifier = Modifier.padding(top = 14.dp)
                        ) {
                            Text(tr("Смотреть", "Watch"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialScreenContent(onBackClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val links = remember {
        listOf(
            SocialLink(
                title = "AstroForum",
                link = "https://astroforum.ru",
                description = "Крупный русскоязычный форум для астрономов-любителей."
            ),
            SocialLink(
                title = "NASA в Instagram",
                link = "https://instagram.com/nasa",
                description = "Официальный аккаунт NASA с анонсами, фото и короткими космическими сводками."
            ),
            SocialLink(
                title = "ESA в YouTube",
                link = "https://www.youtube.com/@EuropeanSpaceAgency",
                description = "Видео о миссиях, телескопах и научных открытиях Европейского космического агентства."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Соцсети астрономов", "Astronomy socials"), onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(links) { link ->
                Card(
                    modifier = Modifier.clickable { uriHandler.openUri(link.link) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(link.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            link.link,
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(link.description, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    currentSettings: AppSettings,
    onBackClick: () -> Unit,
    onApplySettings: (AppSettings, Boolean) -> Unit
) {
    var theme by remember(currentSettings) { mutableStateOf(currentSettings.theme) }
    var language by remember(currentSettings) { mutableStateOf(currentSettings.language) }
    var fontSize by remember(currentSettings) { mutableStateOf(currentSettings.fontSize) }
    var notificationsEnabled by remember(currentSettings) {
        mutableStateOf(currentSettings.notificationsEnabled)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Настройки", "Settings"), onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Тема", "Theme"), fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = theme == "light",
                                onClick = { theme = "light" },
                                label = { Text(tr("Светлая", "Light")) }
                            )
                            FilterChip(
                                selected = theme == "dark",
                                onClick = { theme = "dark" },
                                label = { Text(tr("Темная", "Dark")) }
                            )
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Язык", "Language"), fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = language == "ru",
                                onClick = { language = "ru" },
                                label = { Text("Русский") }
                            )
                            FilterChip(
                                selected = language == "en",
                                onClick = { language = "en" },
                                label = { Text("English") }
                            )
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            tr("Размер шрифта", "Font size") + ": ${fontSize.toInt()}",
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 10f..20f,
                            steps = 9,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("Уведомления", "Notifications"), fontWeight = FontWeight.Bold)
                            Text(
                                tr(
                                    "Включить сводки новостей и напоминания о небесных событиях",
                                    "Enable news digests and sky event reminders"
                                ),
                                modifier = Modifier.padding(top = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onApplySettings(
                                AppSettings(
                                    theme = theme,
                                    language = language,
                                    fontSize = fontSize,
                                    notificationsEnabled = notificationsEnabled
                                ),
                                true
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tr("Сохранить", "Save"))
                    }
                    Button(
                        onClick = {
                            onApplySettings(
                                AppSettings(
                                    theme = theme,
                                    language = language,
                                    fontSize = fontSize,
                                    notificationsEnabled = notificationsEnabled
                                ),
                                false
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tr("Применить", "Apply"))
                    }
                }
            }
        }
    }
}
