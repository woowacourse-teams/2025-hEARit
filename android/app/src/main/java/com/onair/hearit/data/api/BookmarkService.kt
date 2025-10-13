package com.onair.hearit.data.api

import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookmarkService {
    @GET("api/v1/bookmarks")
    suspend fun getBookmarks(
        @Query("page") page: Int? = 0,
        @Query("size") size: Int? = 10,
        @Query("filter") filter: String? = "all",
        @Query("sort") sort: String? = "createdAt,desc",
    ): Response<BookmarkResponse>

    @POST("api/v1/bookmarks/hearits/{hearitId}")
    suspend fun postBookmark(
        @Path("hearitId") hearitId: Long,
    ): Response<BookmarkIdResponse>

    @DELETE("api/v1/bookmarks/{bookmarkId}")
    suspend fun deleteBookmark(
        @Path("bookmarkId") bookmarkId: Long,
    ): Response<Unit>
}
