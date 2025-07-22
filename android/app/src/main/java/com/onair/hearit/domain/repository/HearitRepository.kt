package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.SearchedHearit

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(): Result<List<RandomHearitItem>>

    suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearit>>
}
