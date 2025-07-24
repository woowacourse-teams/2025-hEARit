package com.onair.hearit.data.api

import com.onair.hearit.data.dto.KeywordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KeywordService {
    @GET("keywords/recommend")
    suspend fun getRecommendKeywords(
        @Query("size") size: Int?,
    ): Response<List<KeywordResponse>>
}
