package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse

interface HearitRemoteDataSource {
    suspend fun getHearit(
        token: String?,
        hearitId: Long,
    ): Result<NetworkResult<HearitResponse>>

    suspend fun getRecommendHearits(): Result<NetworkResult<List<RecommendHearitResponse>>>

    suspend fun getRandomHearits(
        token: String?,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<RandomHearitResponse>>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitResponse>>

    suspend fun getCategoryHearits(): Result<NetworkResult<List<GroupedCategoryHearitResponse>>>
}
