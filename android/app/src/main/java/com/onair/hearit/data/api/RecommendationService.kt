package com.onair.hearit.data.api

import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendationService {
    @GET("api/v1/recommendations/categories")
    suspend fun getRecommendationCategories(
        @Query("categorySize") categorySize: Int? = null,
        @Query("hearitSize") hearitSize: Int? = null,
    ): Response<List<RecommendationCategoriesResponse>>
}
