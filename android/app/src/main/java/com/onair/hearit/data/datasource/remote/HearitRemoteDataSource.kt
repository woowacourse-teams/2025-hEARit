package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.SearchHearitsResponse

interface HearitRemoteDataSource {
    suspend fun getHearit(hearitId: Long): NetworkResult<HearitResponse>

    suspend fun getRecommendHearits(): NetworkResult<List<RecommendHearitResponse>>

    suspend fun getExploreHearits(
        cursorId: Long?,
        size: Int?,
    ): NetworkResult<ExploreHearitResponse>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): NetworkResult<SearchHearitsResponse>

    suspend fun getHearits(
        categoryId: Long?,
        page: Int?,
        size: Int?,
    ): NetworkResult<HearitsResponse>

    suspend fun getRecommendationCategoryHearits(): NetworkResult<List<RecommendationCategoriesResponse>>

    suspend fun postHearitView(hearitId: Long): NetworkResult<Unit>
}
