package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.RandomHearitItem
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(): Result<List<RandomHearitItem>>

    suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearit>>
}
