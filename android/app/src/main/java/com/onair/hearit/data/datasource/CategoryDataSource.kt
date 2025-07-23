package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.CategoryResponse

interface CategoryDataSource {
    suspend fun getCategories(): Result<List<CategoryResponse>>
}
