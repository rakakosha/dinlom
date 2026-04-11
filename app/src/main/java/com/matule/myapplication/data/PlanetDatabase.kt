package com.matule.myapplication.data

import com.matule.myapplication.models.Planet

object PlanetDatabase {

    private val planets = listOf(
        Planet(
            id = 1,
            russianName = "Меркурий",
            latinName = "Mercury",
            distanceFromSunAu = 0.39,
            diameterKm = 4879.4,
            moonsCount = 0,
            orbitalPeriodDays = 88.0,
            rotationPeriodHours = 1407.6,
            observationTips = "Наблюдать лучше сразу после заката или перед рассветом над горизонтом.",
            description = "Самая близкая к Солнцу планета с сильно кратерированной поверхностью, похожей на Луну.",
            funFact = "На Меркурии один солнечный день длиннее его года."
        ),
        Planet(
            id = 2,
            russianName = "Венера",
            latinName = "Venus",
            distanceFromSunAu = 0.72,
            diameterKm = 12104.0,
            moonsCount = 0,
            orbitalPeriodDays = 225.0,
            rotationPeriodHours = -5832.5,
            observationTips = "Очень яркая. Лучше всего видна вечером или утром, хорошо показывает фазы в телескоп.",
            description = "Самая горячая планета Солнечной системы из-за плотной атмосферы и мощного парникового эффекта.",
            funFact = "Венера вращается в обратную сторону по сравнению с большинством планет."
        ),
        Planet(
            id = 3,
            russianName = "Земля",
            latinName = "Earth",
            distanceFromSunAu = 1.0,
            diameterKm = 12742.0,
            moonsCount = 1,
            orbitalPeriodDays = 365.25,
            rotationPeriodHours = 24.0,
            observationTips = "Наш дом. Лучше всего наблюдать её с орбиты.",
            description = "Единственная известная планета, на которой существует жизнь и большие объёмы жидкой воды.",
            funFact = "Около 71% поверхности Земли покрыто водой."
        ),
        Planet(
            id = 4,
            russianName = "Марс",
            latinName = "Mars",
            distanceFromSunAu = 1.52,
            diameterKm = 6779.0,
            moonsCount = 2,
            orbitalPeriodDays = 687.0,
            rotationPeriodHours = 24.6,
            observationTips = "Во время противостояний хорошо видны полярные шапки и оттенки поверхности.",
            description = "Красная планета с вулканами, каньонами и следами древней водной активности.",
            funFact = "На Марсе находится Олимп — самая высокая гора в Солнечной системе."
        ),
        Planet(
            id = 5,
            russianName = "Юпитер",
            latinName = "Jupiter",
            distanceFromSunAu = 5.2,
            diameterKm = 139820.0,
            moonsCount = 95,
            orbitalPeriodDays = 4333.0,
            rotationPeriodHours = 9.9,
            observationTips = "В бинокль уже видны четыре галилеевых спутника, а в телескоп — облачные пояса.",
            description = "Газовый гигант с мощной атмосферой и знаменитым Большим красным пятном.",
            funFact = "Юпитер настолько массивен, что его масса больше массы всех остальных планет вместе."
        ),
        Planet(
            id = 6,
            russianName = "Сатурн",
            latinName = "Saturn",
            distanceFromSunAu = 9.58,
            diameterKm = 116460.0,
            moonsCount = 146,
            orbitalPeriodDays = 10759.0,
            rotationPeriodHours = 10.7,
            observationTips = "Даже небольшой телескоп показывает кольца. При хорошей атмосфере заметна щель Кассини.",
            description = "Планета-гигант с самой впечатляющей системой колец в Солнечной системе.",
            funFact = "Плотность Сатурна меньше плотности воды."
        ),
        Planet(
            id = 7,
            russianName = "Уран",
            latinName = "Uranus",
            distanceFromSunAu = 19.2,
            diameterKm = 50724.0,
            moonsCount = 27,
            orbitalPeriodDays = 30687.0,
            rotationPeriodHours = -17.2,
            observationTips = "Для уверенного наблюдения нужен телескоп от 150 мм. Вид выглядит как маленький зеленоватый диск.",
            description = "Ледяной гигант, ось вращения которого почти лежит в плоскости орбиты.",
            funFact = "Уран как будто катится вокруг Солнца лёжа на боку."
        ),
        Planet(
            id = 8,
            russianName = "Нептун",
            latinName = "Neptune",
            distanceFromSunAu = 30.05,
            diameterKm = 49244.0,
            moonsCount = 14,
            orbitalPeriodDays = 60190.0,
            rotationPeriodHours = 16.1,
            observationTips = "Очень тусклый объект, для наблюдения нужен хороший телескоп и тёмное небо.",
            description = "Самая дальняя крупная планета, известная своими сверхбыстрыми ветрами.",
            funFact = "Скорость ветра в атмосфере Нептуна может превышать 2000 км/ч."
        )
    )

    fun getAllPlanets(): List<Planet> = planets

    fun getPlanetById(id: Int): Planet? = planets.find { it.id == id }

    fun getPlanetByName(name: String): Planet? {
        return planets.find {
            it.latinName.equals(name, ignoreCase = true) ||
                it.russianName.equals(name, ignoreCase = true)
        }
    }
}
