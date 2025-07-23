package com.onair.hearit.data.api

import com.onair.hearit.data.dto.CategoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>
}
