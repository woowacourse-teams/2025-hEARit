package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit

interface HearitRepository {
    suspend fun getHearit(hearitId: Long): Result<Hearit>

    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(
        cursorId: Long? = null,
        size: Int? = null,
    ): Result<CursorResult<ExploreHearit>>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<SearchedHearit>>

    suspend fun getCategoryHearits(): Result<List<GroupedCategory>>
}
