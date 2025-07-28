package com.onair.hearit.data.api

import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<CategoryResponse>

    @GET("categories/{categoryId}/hearits")
    suspend fun getHearitsByCategoryId(
        @Path("categoryId") categoryId: Long,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<SearchHearitResponse>
}
