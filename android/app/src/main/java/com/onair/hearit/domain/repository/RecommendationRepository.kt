package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.RecommendationCategories

interface RecommendationRepository {
    suspend fun getRecommendationCategories(): Result<List<RecommendationCategories>>
}
