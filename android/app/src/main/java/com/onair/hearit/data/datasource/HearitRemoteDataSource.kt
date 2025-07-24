package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse

interface HearitRemoteDataSource {
    suspend fun getHearit(hearitId: Long): Result<HearitResponse>

    suspend fun getRecommendHearits(): Result<List<RecommendHearitResponse>>

    suspend fun getRandomHearits(
        page: Int?,
        size: Int?,
    ): Result<RandomHearitResponse>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<SearchHearitResponse>
}
