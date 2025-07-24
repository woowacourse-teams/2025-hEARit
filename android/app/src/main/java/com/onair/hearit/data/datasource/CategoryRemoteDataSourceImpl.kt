package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.dto.CategoryResponse

class CategoryRemoteDataSourceImpl(
    private val categoryService: CategoryService,
) : CategoryRemoteDataSource {
    override suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<CategoryResponse> =
        handleApiCall(
            errorMessage = "카테고리 조회 실패",
            apiCall = { categoryService.getCategories(page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )
}
