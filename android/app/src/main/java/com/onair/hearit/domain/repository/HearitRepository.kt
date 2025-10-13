package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.domain.model.SearchedHearit

interface HearitRepository {
    suspend fun getHearit(hearitId: Long): Result<Hearit>

    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(
        cursorId: Long? = null,
        size: Int? = null,
    ): Result<CursorResult<ExploreHearit>>

    suspend fun getKeywordHearits(
        searchTerm: String,
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<SearchedHearit>>

    suspend fun getCategoryHearits(
        categoryId: Long,
        page: Int? = 0,
        size: Int? = 20,
    ): Result<PageResult<SearchedCategoryHearit>>

    suspend fun getRecentUploadHearits(
        page: Int? = 0,
        size: Int? = 20,
    ): Result<PageResult<RecentUploadHearit>>
}
