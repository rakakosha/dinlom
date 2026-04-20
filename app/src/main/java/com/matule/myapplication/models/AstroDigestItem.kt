package com.matule.myapplication.models

enum class AstroDigestCategory {
    NEWS,
    EVENT,
    KNOWLEDGE
}

data class AstroDigestItem(
    val id: String,
    val category: AstroDigestCategory,
    val title: String,
    val summary: String,
    val dateLabel: String,
    val source: String,
    val sourceUrl: String,
    val tags: List<String> = emptyList()
)
