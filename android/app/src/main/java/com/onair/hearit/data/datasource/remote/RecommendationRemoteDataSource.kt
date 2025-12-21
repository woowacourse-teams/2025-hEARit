package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.RecommendationCategoriesResponse

interface RecommendationRemoteDataSource {
    suspend fun getRecommendationCategories(): NetworkResult<List<RecommendationCategoriesResponse>>
}
