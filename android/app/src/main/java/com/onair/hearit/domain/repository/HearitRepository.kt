package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.SearchedHearit
import com.onair.hearit.domain.SingleHearit

interface HearitRepository {
    suspend fun getHearit(hearitId: Long): Result<SingleHearit>

    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(): Result<List<RandomHearit>>

    suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearit>>
}
