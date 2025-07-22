package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RecommendHearitItem
import com.onair.hearit.domain.SearchedHearitItem

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearitItem>>

    suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearitItem>>
}
