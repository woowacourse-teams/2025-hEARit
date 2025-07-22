package com.onair.hearit.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HearitService {
    @GET("hearits/recommend")
    suspend fun getRecommendHearits(): Response<List<RecommendHearitResponse>>

    @GET("hearits/random")
    suspend fun getRandomHearits(): Response<List<RandomHearitResponse>>

    @GET("hearits/search")
    suspend fun getSearchHearits(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<List<SearchHearitResponse>>
}
