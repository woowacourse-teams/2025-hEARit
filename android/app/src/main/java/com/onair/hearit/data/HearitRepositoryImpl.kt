package com.onair.hearit.data

import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.RecommendHearitItem
import com.onair.hearit.domain.SearchedHearitItem
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
    private val hearitRemoteDataSource: HearitRemoteDataSource,
) : HearitRepository {
    override suspend fun getRecommendHearits(): Result<List<RecommendHearitItem>> =
        handleResult {
            val response = hearitRemoteDataSource.getRecommendHearits().getOrThrow()
            response.map { it.toDomain() }
        }

    override suspend fun getRandomHearits(): Result<List<RandomHearitItem>> =
        handleResult {
            val response = hearitRemoteDataSource.getRandomHearits().getOrThrow()
            response.map { it.toDomain() }
        }

    override suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearitItem>> =
        handleResult {
            val response = hearitRemoteDataSource.getSearchHearits(searchTerm).getOrThrow()
            response.map { it.toDomain() }
        }
}
