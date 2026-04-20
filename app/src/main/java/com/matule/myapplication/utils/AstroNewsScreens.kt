package com.matule.myapplication.utils

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.matule.myapplication.data.AstroDigestRepository
import com.matule.myapplication.models.AstroDigestCategory
import com.matule.myapplication.models.AstroDigestItem

@Composable
fun AstroNewsScreenContent(onBackClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    var searchQuery by remember { mutableStateOf("") }
    val normalizedQuery = searchQuery.trim()
    val upcomingEvents = remember { AstroDigestRepository.getUpcomingEvents() }
    val newsItems = remember { AstroDigestRepository.getNews() }
    val searchResults = remember(normalizedQuery) {
        AstroDigestRepository.search(normalizedQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Астроновости", "Astro news"),
            onBackClick = onBackClick
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(tr("Поиск: Spica, звезда, комета...", "Search: Spica, star, comet...")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (normalizedQuery.isBlank()) {
                item {
                    IntroCard()
                }

                item {
                    SectionTitle(tr("Ближайшие события", "Upcoming events"))
                }

                items(upcomingEvents, key = { it.id }) { item ->
                    AstroDigestCard(item = item, onOpenSource = { openSource(uriHandler, item.sourceUrl) })
                }

                item {
                    SectionTitle(tr("Сводки новостей", "News digests"))
                }

                items(newsItems, key = { it.id }) { item ->
                    AstroDigestCard(item = item, onOpenSource = { openSource(uriHandler, item.sourceUrl) })
                }
            } else {
                item {
                    SectionTitle(
                        tr(
                            "Найдено: ${searchResults.size}",
                            "Found: ${searchResults.size}"
                        )
                    )
                }

                if (searchResults.isEmpty()) {
                    item {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    tr("Совпадений не найдено", "No matches found"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    tr(
                                        "Попробуйте запросы вроде Spica, звезда, комета, галактика, Hubble или Euclid.",
                                        "Try queries like Spica, star, comet, galaxy, Hubble or Euclid."
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(searchResults, key = { it.id }) { item ->
                        AstroDigestCard(item = item, onOpenSource = { openSource(uriHandler, item.sourceUrl) })
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = tr("Что внутри вкладки", "What this tab contains"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = tr(
                    "Здесь собраны краткие новости по астрономии, ближайшие наблюдательные события и справочные карточки по объектам. Поиск работает и по заголовкам, и по ключевым словам.",
                    "Here you can find short astronomy news digests, upcoming observing events, and quick reference cards. Search works across titles and keywords."
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun AstroDigestCard(
    item: AstroDigestItem,
    onOpenSource: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(categoryLabel(item.category)) }
                )
                Text(
                    text = item.dateLabel,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = item.summary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = tr("Источник", "Source") + ": ${item.source}",
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            if (item.tags.isNotEmpty()) {
                Text(
                    text = item.tags.take(5).joinToString(prefix = "#", separator = "  #"),
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (item.sourceUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenSource) {
                    Text(tr("Открыть источник", "Open source"))
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: AstroDigestCategory): String {
    return when (category) {
        AstroDigestCategory.NEWS -> tr("Новость", "News")
        AstroDigestCategory.EVENT -> tr("Событие", "Event")
        AstroDigestCategory.KNOWLEDGE -> tr("Справка", "Reference")
    }
}

private fun openSource(uriHandler: androidx.compose.ui.platform.UriHandler, url: String) {
    if (url.isNotBlank()) {
        uriHandler.openUri(url)
    }
}
