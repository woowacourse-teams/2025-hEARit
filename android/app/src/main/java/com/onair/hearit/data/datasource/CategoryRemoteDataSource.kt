package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.CategoryResponse

interface CategoryRemoteDataSource {
    suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<CategoryResponse>
}
