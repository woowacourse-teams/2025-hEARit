package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.dto.CategoryResponse

class CategoryDataSourceImpl(
    private val categoryService: CategoryService,
) : CategoryDataSource {
    override suspend fun getCategories(): Result<List<CategoryResponse>> =
        handleApiCall(
            errorMessage = "카테고리 조회 실패",
            apiCall = { categoryService.getCategories() },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )
}
