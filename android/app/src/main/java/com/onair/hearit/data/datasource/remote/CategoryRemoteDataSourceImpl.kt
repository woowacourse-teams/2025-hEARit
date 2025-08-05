package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.data.dto.SearchHearitResponse

class CategoryRemoteDataSourceImpl(
    private val categoryService: CategoryService,
    private val errorResponseHandler: ErrorResponseHandler,
) : CategoryRemoteDataSource {
    override suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<CategoryResponse>> =
        handleApiCall(
            apiCall = { categoryService.getCategories(page, size) },
            transform = { response ->
                response.body()
                    ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getHearitsByCategoryId(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitResponse>> =
        handleApiCall(
            apiCall = { categoryService.getHearitsByCategoryId(categoryId, page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
