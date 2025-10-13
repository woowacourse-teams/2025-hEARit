package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.SearchHearitsResponse

interface HearitRemoteDataSource {
    suspend fun getHearit(hearitId: Long): Result<NetworkResult<HearitResponse>>

    suspend fun getRecommendHearits(): Result<NetworkResult<List<RecommendHearitResponse>>>

    suspend fun getRandomHearits(
        cursorId: Long?,
        size: Int?,
    ): Result<NetworkResult<ExploreHearitResponse>>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitsResponse>>

    suspend fun getHearits(
        categoryId: Long?,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<HearitsResponse>>

    suspend fun getRecommendationCategoryHearits(): Result<NetworkResult<List<RecommendationCategoriesResponse>>>
}
