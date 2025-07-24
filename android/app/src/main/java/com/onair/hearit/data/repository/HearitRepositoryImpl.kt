package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.HearitRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit
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

    override suspend fun getRandomHearits(
        page: Int?,
        size: Int?,
    ): Result<PageResult<RandomHearit>> =
        handleResult {
            hearitRemoteDataSource.getRandomHearits(page, size).getOrThrow().toDomain()
        }

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>> =
        handleResult {
            hearitRemoteDataSource.getSearchHearits(searchTerm, page, size).getOrThrow().toDomain()
        }
}
