package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.CategoryService
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
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
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
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
            errorHandler = errorResponseHandler,
        )
}
