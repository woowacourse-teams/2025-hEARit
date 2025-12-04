package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val categoryRemoteDataSource: CategoryRemoteDataSource,
) : CategoryRepository {
    override suspend fun getCategories(
        page: Int?,
        size: Int?,
    ): Result<PageResult<Category>> = categoryRemoteDataSource.getCategories(page, size).toDomainResult { it.toDomain() }
}
