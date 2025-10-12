package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.mapper.toRecentUploadHearit
import com.onair.hearit.data.mapper.toSearchedCategoryHearit
import com.onair.hearit.data.mapper.toSearchedHearit
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
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

    override suspend fun getKeywordHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>> =
        hearitRemoteDataSource
            .getSearchHearits(searchTerm, page, size)
            .mapOrThrowDomain { it.toSearchedHearit() }

    override suspend fun getCategoryHearits(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedCategoryHearit>> =
        hearitRemoteDataSource
            .getHearits(categoryId, page, size)
            .mapOrThrowDomain { it.toSearchedCategoryHearit() }

    override suspend fun getRecentUploadHearits(
        page: Int?,
        size: Int?,
    ): Result<PageResult<RecentUploadHearit>> =
        hearitRemoteDataSource
            .getHearits(null, page, size)
            .mapOrThrowDomain { it.toRecentUploadHearit() }
}
