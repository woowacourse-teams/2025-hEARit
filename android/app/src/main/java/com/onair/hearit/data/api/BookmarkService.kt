package com.onair.hearit.data.api

import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookmarkService {
    @GET("bookmarks/hearits")
    suspend fun getBookmarks(
        @Header("Authorization") token: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<BookmarkResponse>

    @POST("bookmarks/hearits/{hearitId}")
    suspend fun postBookmark(
        @Header("Authorization") token: String?,
        @Path("hearitId") hearitId: Long,
    ): Response<BookmarkIdResponse>

    @DELETE("bookmarks/{bookmarkId}")
    suspend fun deleteBookmark(
        @Header("Authorization") token: String?,
        @Path("bookmarkId") bookmarkId: Long,
    ): Response<Unit>
}
