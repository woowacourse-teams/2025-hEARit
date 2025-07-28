package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.SearchedHearit

interface CategoryRepository {
    suspend fun getCategories(
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<Category>>

    suspend fun getHearitsByCategoryId(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>>
}
