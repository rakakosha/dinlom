package com.matule.myapplication.data

import com.matule.myapplication.models.Planet

object PlanetDatabase {

    fun getAllPlanets(): List<Planet> {
        return listOf(
            Planet(
                id = 1,
                russianName = "Меркурий",
                latinName = "Mercury",
                distanceFromSunAu = 0.39,
                diameterKm = 4879.4,
                moonsCount = 0,
                observationTips = "Виден низко над горизонтом, лучше всего наблюдать сразу после заката",
                description = "Самая близкая к Солнцу планета. Поверхность покрыта кратерами, напоминает Луну."
            ),
            Planet(
                id = 2,
                russianName = "Венера",
                latinName = "Venus",
                distanceFromSunAu = 0.72,
                diameterKm = 12104.0,
                moonsCount = 0,
                observationTips = "Очень яркая, видна вечером или утром. Лучше всего наблюдать в телескоп с синим фильтром",
                description = "Самая горячая планета из-за парникового эффекта. Покрыта плотными облаками серной кислоты."
            ),
            Planet(
                id = 3,
                russianName = "Земля",
                latinName = "Earth",
                distanceFromSunAu = 1.0,
                diameterKm = 12742.0,
                moonsCount = 1,
                observationTips = "Наш дом. Наблюдать можно только из космоса :)",
                description = "Единственная известная планета с жизнью. 71% поверхности покрыто водой."
            ),
            Planet(
                id = 4,
                russianName = "Марс",
                latinName = "Mars",
                distanceFromSunAu = 1.52,
                diameterKm = 6779.0,
                moonsCount = 2,
                observationTips = "Красноватый оттенок. Хорошо видны полярные шапки в телескоп",
                description = "Красная планета с самой высокой горой в Солнечной системе - Олимп (22 км)."
            ),
            Planet(
                id = 5,
                russianName = "Юпитер",
                latinName = "Jupiter",
                distanceFromSunAu = 5.2,
                diameterKm = 139820.0,
                moonsCount = 79,
                observationTips = "В бинокль видны 4 галилеевых спутника. В телескоп - облачные пояса",
                description = "Самая большая планета. Имеет большое красное пятно - гигантский шторм."
            ),
            Planet(
                id = 6,
                russianName = "Сатурн",
                latinName = "Saturn",
                distanceFromSunAu = 9.58,
                diameterKm = 116460.0,
                moonsCount = 82,
                observationTips = "Знаменитые кольца видны даже в небольшой телескоп",
                description = "Обладает самой развитой системой колец. Плотность меньше воды - утонул бы в океане."
            ),
            Planet(
                id = 7,
                russianName = "Уран",
                latinName = "Uranus",
                distanceFromSunAu = 19.2,
                diameterKm = 50724.0,
                moonsCount = 27,
                observationTips = "Виден как маленький зеленоватый диск. Нужен телескоп с апертурой от 150 мм",
                description = "Вращается 'лёжа на боку'. Имеет слабые кольца."
            ),
            Planet(
                id = 8,
                russianName = "Нептун",
                latinName = "Neptune",
                distanceFromSunAu = 30.05,
                diameterKm = 49244.0,
                moonsCount = 14,
                observationTips = "Очень тусклый. Для наблюдения нужен мощный телескоп",
                description = "Самая ветреная планета со скоростью ветра до 2100 км/ч."
            )
        )
    }

    fun getPlanetById(id: Int): Planet? {
        return getAllPlanets().find { it.id == id }
    }
}