package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.CategoryRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val categoryDataSource: CategoryRemoteDataSource,
) : CategoryRepository {
    override suspend fun getCategories(): Result<List<Category>> =
        handleResult {
            val response = categoryDataSource.getCategories().getOrThrow()
            response.map { it.toDomain() }
        }
}
