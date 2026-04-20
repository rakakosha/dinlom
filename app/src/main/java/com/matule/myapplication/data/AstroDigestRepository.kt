package com.matule.myapplication.data

import com.matule.myapplication.models.AstroDigestCategory
import com.matule.myapplication.models.AstroDigestItem
import java.util.Locale

object AstroDigestRepository {

    private val digestItems = listOf(
        AstroDigestItem(
            id = "news_hubble_constant_2026_04_13",
            category = AstroDigestCategory.NEWS,
            title = "Уточнена скорость расширения Вселенной",
            summary = "Международная группа объединила несколько ступеней космической шкалы расстояний и получила более точную локальную оценку постоянной Хаббла: около 73.5 км/с/Мпк. Это поддерживает идею, что расширение современной Вселенной идёт быстрее, чем следует из ранней модели после Большого взрыва.",
            dateLabel = "13 Apr 2026",
            source = "NASA Science",
            sourceUrl = "https://science.nasa.gov/blogs/science-news/2026/04/13/international-collaboration-helps-pinpoint-universes-expansion-rate/",
            tags = listOf(
                "hubble",
                "webb",
                "expansion",
                "universe",
                "hubble constant",
                "вселенная",
                "расширение",
                "астрофизика"
            )
        ),
        AstroDigestItem(
            id = "news_hubble_ic486_2026_04_13",
            category = AstroDigestCategory.NEWS,
            title = "Hubble показал активную спиральную галактику IC 486",
            summary = "Новый снимок Hubble показывает барred-спираль IC 486 в созвездии Близнецов на расстоянии около 380 миллионов световых лет. В центре заметно активное ядро, питаемое сверхмассивной чёрной дырой, а по диску видны пылевые полосы и области недавнего звездообразования.",
            dateLabel = "13 Apr 2026",
            source = "NASA Hubble",
            sourceUrl = "https://science.nasa.gov/missions/hubble/hubble-spies-an-active-spiral/",
            tags = listOf(
                "hubble",
                "galaxy",
                "ic 486",
                "agn",
                "black hole",
                "галактика",
                "черная дыра",
                "близнецы"
            )
        ),
        AstroDigestItem(
            id = "news_dart_didymos_2026_03_06",
            category = AstroDigestCategory.NEWS,
            title = "DART впервые заметно изменила орбиту небесного тела вокруг Солнца",
            summary = "Анализ NASA показал, что удар аппарата DART по Диморфу изменил не только его движение вокруг Дидима, но и орбиту всей двойной системы вокруг Солнца. Это стало первым измеримым случаем, когда созданный человеком объект изменил солнечную орбиту другого небесного тела.",
            dateLabel = "06 Mar 2026",
            source = "NASA",
            sourceUrl = "https://www.nasa.gov/missions/dart/nasas-dart-mission-changed-orbit-of-asteroid-didymos-around-sun",
            tags = listOf(
                "dart",
                "didymos",
                "dimorphos",
                "asteroid",
                "planetary defense",
                "астероид",
                "защита планеты"
            )
        ),
        AstroDigestItem(
            id = "news_cosmic_eye_2026_03_03",
            category = AstroDigestCategory.NEWS,
            title = "Hubble и Euclid вместе рассмотрели туманность Кошачий Глаз",
            summary = "ESA опубликовало совместный обзор туманности NGC 6543: Euclid показал широкое поле с удалёнными галактиками вокруг объекта, а Hubble детально раскрыл сложные оболочки, струи газа и следы поздних стадий эволюции звезды.",
            dateLabel = "03 Mar 2026",
            source = "ESA",
            sourceUrl = "https://www.esa.int/Science_Exploration/Space_Science/Hubble_Euclid_zoom_into_cosmic_eye",
            tags = listOf(
                "euclid",
                "hubble",
                "cat's eye",
                "nebula",
                "planetary nebula",
                "туманность",
                "кошачий глаз",
                "draco"
            )
        ),
        AstroDigestItem(
            id = "news_dark_galaxy_2026_02_18",
            category = AstroDigestCategory.NEWS,
            title = "Найдена почти тёмная галактика CDG-2",
            summary = "Комбинация наблюдений Hubble, Euclid и Subaru помогла выделить кандидат в крайне тёмную галактику CDG-2. Объект содержит очень мало звёзд, но при этом, по оценке исследователей, может быть почти полностью доминирован тёмной материей.",
            dateLabel = "18 Feb 2026",
            source = "ESA",
            sourceUrl = "https://www.esa.int/Science_Exploration/Space_Science/Hubble_Euclid_Subaru_uncover_dark_galaxy",
            tags = listOf(
                "dark matter",
                "dark galaxy",
                "euclid",
                "hubble",
                "subaru",
                "темная материя",
                "галактика"
            )
        ),
        AstroDigestItem(
            id = "event_lyrids_2026_04_21",
            category = AstroDigestCategory.EVENT,
            title = "Пик метеорного потока Лириды",
            summary = "NASA советует наблюдать Лириды ночью с 21 на 22 апреля. Лучше смотреть на восток после 22:00, а радиант находится рядом с Вегой в созвездии Лиры.",
            dateLabel = "21-22 Apr 2026",
            source = "NASA Skywatching",
            sourceUrl = "https://science.nasa.gov/solar-system/whats-up-april-2026-skywatching-tips-from-nasa/",
            tags = listOf(
                "lyrids",
                "meteor shower",
                "vega",
                "lyra",
                "метеоры",
                "лириды",
                "вега"
            )
        ),
        AstroDigestItem(
            id = "event_comet_r3_2026_04_27",
            category = AstroDigestCategory.EVENT,
            title = "Комета C/2025 R3 пройдёт ближе всего к Земле",
            summary = "Комета C/2025 R3 выходит на минимальную дистанцию к Земле 27 апреля. NASA отмечает, что в конце апреля её удобнее искать в предрассветные часы с биноклем или телескопом.",
            dateLabel = "27 Apr 2026",
            source = "NASA Skywatching",
            sourceUrl = "https://science.nasa.gov/solar-system/whats-up-april-2026-skywatching-tips-from-nasa/",
            tags = listOf(
                "comet",
                "c/2025 r3",
                "pegasus",
                "pisces",
                "комета",
                "апрель",
                "наблюдение"
            )
        ),
        AstroDigestItem(
            id = "event_flower_moon_2026_05_01",
            category = AstroDigestCategory.EVENT,
            title = "1 мая — майское полнолуние",
            summary = "Первое майское полнолуние наступает 1 мая 2026 года. По данным timeanddate, май в 2026 году особенный: в нём будет сразу два полнолуния, а значит в конце месяца ожидается Blue Moon.",
            dateLabel = "01 May 2026",
            source = "timeanddate.com",
            sourceUrl = "https://www.timeanddate.com/news/astronomy/moon-guide-2026",
            tags = listOf(
                "full moon",
                "flower moon",
                "blue moon",
                "moon",
                "полнолуние",
                "луна",
                "май"
            )
        ),
        AstroDigestItem(
            id = "event_eta_aquariids_2026_05_05",
            category = AstroDigestCategory.EVENT,
            title = "Eta Aquariids лучше всего смотреть перед рассветом 5 мая",
            summary = "Поток Eta Aquariids достигает максимума ранним утром 5 мая. Быстрые метеоры связаны с кометой Галлея, а лучший обзор обычно получается перед рассветом, особенно вдали от городской засветки.",
            dateLabel = "05 May 2026",
            source = "EarthSky / NASA reference",
            sourceUrl = "https://earthsky.org/astronomy-essentials/everything-you-need-to-know-eta-aquarid-meteor-shower/",
            tags = listOf(
                "eta aquariids",
                "meteor shower",
                "halley",
                "aquarius",
                "метеорный поток",
                "галлей",
                "эта-аквариды"
            )
        ),
        AstroDigestItem(
            id = "knowledge_spica",
            category = AstroDigestCategory.KNOWLEDGE,
            title = "Спика",
            summary = "Спика — самая яркая звезда созвездия Девы и один из главных весенних ориентиров на небе. Если в поиске ввести Spica или «звезда Девы», приложение покажет эту карточку и связанные события.",
            dateLabel = "Справка",
            source = "Справка приложения",
            sourceUrl = "",
            tags = listOf(
                "spica",
                "alpha virginis",
                "virgo",
                "star",
                "bright star",
                "спика",
                "звезда",
                "дева"
            )
        )
    )

    fun getNews(): List<AstroDigestItem> =
        digestItems.filter { it.category == AstroDigestCategory.NEWS }

    fun getUpcomingEvents(): List<AstroDigestItem> =
        digestItems.filter { it.category == AstroDigestCategory.EVENT }

    fun search(query: String): List<AstroDigestItem> {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.isBlank()) return emptyList()

        return digestItems.filter { item ->
            buildString {
                append(item.title)
                append(' ')
                append(item.summary)
                append(' ')
                append(item.source)
                append(' ')
                append(item.dateLabel)
                append(' ')
                append(item.tags.joinToString(" "))
            }.lowercase(Locale.ROOT).contains(normalizedQuery)
        }
    }
}
