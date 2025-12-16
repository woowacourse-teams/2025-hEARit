package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.CategoryResponse

interface CategoryRemoteDataSource {
    suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): NetworkResult<CategoryResponse>
}
