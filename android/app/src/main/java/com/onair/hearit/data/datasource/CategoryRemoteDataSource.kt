package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.CategoryResponse

interface CategoryRemoteDataSource {
    suspend fun getCategories(): Result<List<CategoryResponse>>
}
