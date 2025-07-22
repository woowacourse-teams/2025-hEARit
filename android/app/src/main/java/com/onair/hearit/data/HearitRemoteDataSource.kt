package com.onair.hearit.data

interface HearitRemoteDataSource {
    suspend fun getRecommendHearits(): Result<List<RecommendHearitResponse>>
}
