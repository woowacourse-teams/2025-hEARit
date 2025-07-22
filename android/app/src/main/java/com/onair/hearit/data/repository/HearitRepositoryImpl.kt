package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.HearitRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.SearchedHearit
import com.onair.hearit.domain.SingleHearit
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
    private val hearitRemoteDataSource: HearitRemoteDataSource,
) : HearitRepository {
    override suspend fun getHearit(hearitId: Long): Result<SingleHearit> =
        handleResult {
            val response = hearitRemoteDataSource.getHearit(hearitId).getOrThrow()
            response.toDomain()
        }

    override suspend fun getRecommendHearits(): Result<List<RecommendHearit>> =
        handleResult {
            val response = hearitRemoteDataSource.getRecommendHearits().getOrThrow()
            response.map { it.toDomain() }
        }

    override suspend fun getRandomHearits(): Result<List<RandomHearit>> =
        handleResult {
            val response = hearitRemoteDataSource.getRandomHearits().getOrThrow()
            response.map { it.toDomain() }
        }

    override suspend fun getSearchHearits(searchTerm: String): Result<List<SearchedHearit>> =
        handleResult {
            val response = hearitRemoteDataSource.getSearchHearits(searchTerm).getOrThrow()
            response.map { it.toDomain() }
        }
}
