package com.onair.hearit.domain.repository

import com.onair.hearit.domain.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}
