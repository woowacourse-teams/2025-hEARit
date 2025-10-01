package com.onair.hearit.data.api

import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.PlayingBookmarkResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecentUploadResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HearitService {
    @GET("api/v1/hearits/recommend")
    suspend fun getRecommendHearits(): Response<List<RecommendHearitResponse>>

    @GET("api/v1/hearits/recommend-category")
    suspend fun getCategoryHearits(): Response<List<GroupedCategoryHearitResponse>>

    @GET("api/v1/hearits/recent")
    suspend fun getRecentUploadHearits(): Response<List<RecentUploadResponse>>

    //    @GET("api/v1/hearits/playing-bookmarks")
    suspend fun getPlayingBookmarkHearits(): Response<List<PlayingBookmarkResponse>>

    @GET("api/v2/hearits/explore")
    suspend fun getRandomHearits(
        @Query("cursorId") cursorId: Long?,
        @Query("size") size: Int?,
    ): Response<RandomHearitResponse>

    @GET("api/v1/hearits/search")
    suspend fun getSearchHearits(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<SearchHearitResponse>

    @GET("api/v1/hearits/{hearitId}")
    suspend fun getHearit(
        @Path("hearitId") hearitId: Long,
    ): Response<HearitResponse>
}
