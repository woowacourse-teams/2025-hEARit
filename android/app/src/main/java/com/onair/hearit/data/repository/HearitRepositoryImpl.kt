package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.mapper.toRecentUploadHearit
import com.onair.hearit.data.mapper.toSearchedCategoryHearit
import com.onair.hearit.data.mapper.toSearchedHearit
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.HearitRepository

class HearitRepositoryImpl(
    private val hearitRemoteDataSource: HearitRemoteDataSource,
) : HearitRepository {
    override suspend fun getHearit(hearitId: Long): Result<Hearit> =
        hearitRemoteDataSource.getHearit(hearitId).toDomainResult { it.toDomain() }

    override suspend fun getRecommendHearits(): Result<List<RecommendHearit>> =
        hearitRemoteDataSource.getRecommendHearits().toDomainResultList { it.toDomain() }

    override suspend fun getExploreHearits(
        cursorId: Long?,
        size: Int?,
    ): Result<CursorResult<ExploreHearit>> =
        hearitRemoteDataSource
            .getExploreHearits(cursorId, size)
            .toDomainResult { it.toDomain() }

    override suspend fun getKeywordHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>> =
        hearitRemoteDataSource
            .getSearchHearits(searchTerm, page, size)
            .toDomainResult { it.toSearchedHearit() }

    override suspend fun getCategoryHearits(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedCategoryHearit>> =
        hearitRemoteDataSource
            .getHearits(categoryId, page, size)
            .toDomainResult { it.toSearchedCategoryHearit() }

    override suspend fun getRecentUploadHearits(
        page: Int?,
        size: Int?,
    ): Result<PageResult<RecentUploadHearit>> =
        hearitRemoteDataSource
            .getHearits(null, page, size)
            .toDomainResult { it.toRecentUploadHearit() }
}
