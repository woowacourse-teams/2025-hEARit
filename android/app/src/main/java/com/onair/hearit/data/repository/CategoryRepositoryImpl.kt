package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.CategoryRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val categoryDataSource: CategoryRemoteDataSource,
) : CategoryRepository {
    override suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<PageResult<Category>> =
        handleResult {
            categoryDataSource.getCategories(page, size).getOrThrow().toDomain()
        }

    override suspend fun getHearitsByCategoryId(
        categoryId: Long,
        page: Int?,
        size: Int?,
    ): Result<PageResult<SearchedHearit>> =
        handleResult {
            categoryDataSource
                .getHearitsByCategoryId(categoryId, page, size)
                .getOrThrow()
                .toDomain()
        }
}
