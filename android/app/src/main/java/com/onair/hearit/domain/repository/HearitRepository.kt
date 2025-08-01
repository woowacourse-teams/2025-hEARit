package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit

interface HearitRepository {
    suspend fun getHearit(hearitId: Long): Result<SingleHearit>

    suspend fun getRecommendHearits(): Result<List<RecommendHearit>>

    suspend fun getRandomHearits(
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<RandomHearit>>

    suspend fun getSearchHearits(
        searchTerm: String,
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<SearchedHearit>>

    suspend fun getCategoryHearits(): Result<List<GroupedCategory>>
}
