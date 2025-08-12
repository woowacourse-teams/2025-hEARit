package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
    private val hearitRemoteDataSource: HearitRemoteDataSource,
) : HearitRepository {
    override suspend fun getHearit(hearitId: Long): Result<SingleHearit> =
        hearitRemoteDataSource.getHearit(hearitId).mapOrThrowDomain { it.toDomain() }

    override suspend fun getRecommendHearits(): Result<List<RecommendHearit>> =
        hearitRemoteDataSource.getRecommendHearits().mapListOrThrowDomain { it.toDomain() }

    override suspend fun getRandomHearits(
        cursorId: Long?,
        size: Int?,
    ): Result<CursorResult<RandomHearit>> =
        hearitRemoteDataSource
            .getRandomHearits(cursorId, size)
            .mapOrThrowDomain { it.toDomain() }

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>> =
        hearitRemoteDataSource
            .getSearchHearits(searchTerm, page, size)
            .mapOrThrowDomain { it.toDomain() }

    override suspend fun getCategoryHearits(): Result<List<GroupedCategory>> =
        hearitRemoteDataSource.getCategoryHearits().mapListOrThrowDomain { it.toDomain() }
}
