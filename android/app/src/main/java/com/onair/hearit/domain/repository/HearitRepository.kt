package com.onair.hearit.domain.repository

import com.onair.hearit.domain.RecommendHearitItem

interface HearitRepository {
    suspend fun getRecommendHearits(): Result<List<RecommendHearitItem>>
}
