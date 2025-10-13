package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult

interface CategoryRepository {
    suspend fun getCategories(
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<Category>>
}
