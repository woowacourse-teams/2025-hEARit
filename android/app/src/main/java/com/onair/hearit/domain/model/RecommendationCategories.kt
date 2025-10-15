package com.onair.hearit.domain.model

data class RecommendationCategories(
    val categoryId: Long,
    val categoryName: String,
    val colorCode: String,
    val hearits: List<CategoryHearit>,
)
