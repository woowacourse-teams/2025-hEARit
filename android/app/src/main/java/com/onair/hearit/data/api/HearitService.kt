package com.onair.hearit.data.api

import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HearitService {
    @GET("hearits/{hearitId}")
    suspend fun getHearit(
        @Path("hearitId") hearitId: Long,
    ): Response<HearitResponse>

    @GET("hearits/recommend")
    suspend fun getRecommendHearits(): Response<List<RecommendHearitResponse>>

    @GET("hearits/random")
    suspend fun getRandomHearits(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<RandomHearitResponse>

    @GET("hearits/search")
    suspend fun getSearchHearits(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<SearchHearitResponse>
}
