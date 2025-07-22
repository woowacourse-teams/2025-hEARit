package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.RecommendHearitItem
import com.onair.hearit.domain.SearchedHearitItem

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearitItem>>

    suspend fun getRandomHearits(): Result<List<RandomHearitItem>>

    suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearitItem>>
}
