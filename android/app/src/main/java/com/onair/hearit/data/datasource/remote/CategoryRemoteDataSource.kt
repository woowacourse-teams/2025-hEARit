package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.data.dto.SearchHearitResponse

interface CategoryRemoteDataSource {
    suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<CategoryResponse>>

    suspend fun getHearitsByCategoryId(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitResponse>>
}
