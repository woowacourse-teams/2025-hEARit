package com.onair.hearit.data

import com.onair.hearit.domain.RecommendHearitItem
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
    private val hearitRemoteDataSource: HearitRemoteDataSource,
) : HearitRepository {
    override suspend fun getRecommendHearits(): Result<List<RecommendHearitItem>> =
        handleResult {
            val response = hearitRemoteDataSource.getRecommendHearits().getOrThrow()
            response.map { it.toDomain() }
        }
}
