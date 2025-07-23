package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.SearchedHearit

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(
        page: Int? = null,
        size: Int? = null,
    ): Result<List<RandomHearit>>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int? = null,
        size: Int? = null,
    ): Result<List<SearchedHearit>>
}
