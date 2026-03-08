package com.matule.myapplication.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Возвращает текущую дату и время в формате "dd.MM.yyyy HH:mm"
 */
fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date())
}

/**
 * Форматирует дату в строку
 */
fun formatDate(date: Date, pattern: String = "dd.MM.yyyy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * Форматирует дату и время в строку
 */
fun formatDateTime(date: Date, pattern: String = "dd.MM.yyyy HH:mm"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * Парсит строку в дату
 */
fun parseDate(dateString: String, pattern: String = "dd.MM.yyyy"): Date? {
    return try {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        dateFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

/**
 * Получает начало дня (00:00:00)
 */
fun startOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Получает конец дня (23:59:59)
 */
fun endOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

/**
 * Добавляет дни к дате
 */
fun addDays(date: Date, days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

/**
 * Разница в днях между двумя датами
 */
fun daysBetween(date1: Date, date2: Date): Long {
    val diff = date2.time - date1.time
    return diff / (24 * 60 * 60 * 1000)
}