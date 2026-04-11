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
    "1. Распаковка и проверка комплектации",
    "Труба, монтировка, штатив, окуляры 10 мм и 25 мм, искатель.",
    "2. Сборка штатива и монтировки",
    "Разложите штатив, выставьте высоту ног и проверьте плавность осей.",
    "3. Установка оптической трубы",
    "Закрепите трубу на монтировке и начните с окуляра 25 мм.",
    "4. Настройка искателя",
    "Днем наведитесь на удаленный объект и совместите его с центром окуляра.",
    "5. Первые наблюдения",
    "Начните с Луны, затем ищите Венеру, Юпитер и Сатурн.",
    "6. Фокусировка и уход",
    "После наблюдений закрывайте трубу крышкой и храните оптику в сухом месте."
)

private fun getTelescopeInfo(aperture: Int): String {
    return when {
        aperture < 60 -> "Телескоп с апертурой $aperture мм подходит для Луны, ярких планет и двойных звёзд."
        aperture <= 80 -> "Телескоп $aperture мм уже покажет кольца Сатурна, спутники Юпитера и яркие туманности."
        aperture <= 150 -> "Телескоп $aperture мм подходит для большинства объектов Мессье и тонких деталей Луны."
        aperture <= 2000 -> "Телескоп $aperture мм раскрывает слабые галактики, туманности и мелкие детали планет."
        else -> "Телескоп $aperture мм впечатляет сам по себе. Похоже, вы готовы к наблюдениям профессионального уровня."
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
        ScreenTopBar(title = tr("Памятка по телескопам", "Telescope guide"), onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Введите апертуру телескопа в миллиметрах", "Enter telescope aperture in millimeters"), fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = apertureText,
                            onValueChange = { apertureText = it.filter(Char::isDigit) },
                            label = { Text(tr("Апертура", "Aperture")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                        Text(
                            telescopeInfo ?: tr("Введите корректное число, например 70, 100 или 150.", "Enter a valid number like 70, 100 or 150."),
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
            VideoItem("Строение Вселенной (NASA)", "15:32", "Увлекательное путешествие по масштабам Вселенной.", "https://www.youtube.com/@NASA"),
            VideoItem("Как рождаются звёзды", "22:15", "Кратко о звездообразовании, туманностях и молодых звёздных скоплениях.", "https://www.youtube.com/@ESOobservatory"),
            VideoItem("Топ-5 космических явлений", "10:45", "Подборка самых впечатляющих наблюдаемых космических феноменов.", "https://www.youtube.com/@NASAHubble")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Видео о звёздах", "Space videos"), onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(tr("Длительность", "Duration") + ": ${video.duration}", modifier = Modifier.padding(top = 4.dp))
                        Text(video.description, modifier = Modifier.padding(top = 10.dp))
                        Button(onClick = { uriHandler.openUri(video.link) }, modifier = Modifier.padding(top = 14.dp)) {
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
            SocialLink("AstroForum", "https://astroforum.ru", "Крупный русскоязычный форум астрономов-любителей."),
            SocialLink("NASA в Instagram", "https://instagram.com/nasa", "Официальный аккаунт NASA с космическими фото и анонсами."),
            SocialLink("AstroChannel", "https://youtube.com/astrochannel", "Канал с роликами о космосе, наблюдениях и технике.")
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
                        Text(link.link, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.primary)
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
                        Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = theme == "light", onClick = { theme = "light" }, label = { Text(tr("Светлая", "Light")) })
                            FilterChip(selected = theme == "dark", onClick = { theme = "dark" }, label = { Text(tr("Темная", "Dark")) })
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Язык", "Language"), fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = language == "ru", onClick = { language = "ru" }, label = { Text("Русский") })
                            FilterChip(selected = language == "en", onClick = { language = "en" }, label = { Text("English") })
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Размер шрифта", "Font size") + ": ${fontSize.toInt()}", fontWeight = FontWeight.Bold)
                        Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 10f..20f, steps = 9, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onApplySettings(AppSettings(theme = theme, language = language, fontSize = fontSize), true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tr("Сохранить", "Save"))
                    }
                    Button(
                        onClick = { onApplySettings(AppSettings(theme = theme, language = language, fontSize = fontSize), false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tr("Применить", "Apply"))
                    }
                }
            }
        }
    }
}
