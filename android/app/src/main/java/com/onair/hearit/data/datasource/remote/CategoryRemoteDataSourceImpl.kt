package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.CategoryResponse
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val categoryService: CategoryService,
    private val errorResponseHandler: ErrorResponseHandler,
) : CategoryRemoteDataSource {
    override suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): NetworkResult<CategoryResponse> =
        handleApiCall(
            apiCall = { categoryService.getCategories(page, size) },
            errorHandler = errorResponseHandler,
        )
}
