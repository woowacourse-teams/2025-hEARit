package com.onair.hearit.data.api

import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.SearchHearitsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HearitService {
    @GET("api/v1/hearits/recommend")
    suspend fun getRecommendHearits(): Response<List<RecommendHearitResponse>>

    @GET("api/v1/hearits/recommend-category")
    suspend fun getCategoryHearits(): Response<List<RecommendationCategoriesResponse>>

    @GET("api/v1/hearits")
    suspend fun getHearits(
        @Query("categoryId") categoryId: Long? = null,
        @Query("sort") sort: String? = "createdAt,desc",
        @Query("page") page: Int? = 0,
        @Query("size") size: Int? = 0,
    ): Response<HearitsResponse>

    @GET("api/v2/hearits/explore")
    suspend fun getExploreHearits(
        @Query("cursorId") cursorId: Long?,
        @Query("size") size: Int?,
    ): Response<ExploreHearitResponse>

    @GET("api/v1/hearits/search")
    suspend fun getSearchHearits(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<SearchHearitsResponse>

    @GET("api/v1/hearits/{hearitId}")
    suspend fun getHearit(
        @Path("hearitId") hearitId: Long,
    ): Response<HearitResponse>

    @POST("api/v1/hearits/{hearitId}/view")
    suspend fun postHearitView(
        @Path("hearitId") hearitId: Long,
    ): Response<Unit>
}
