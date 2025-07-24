package com.onair.hearit.data.api

import com.onair.hearit.data.dto.CategoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<CategoryResponse>
}
