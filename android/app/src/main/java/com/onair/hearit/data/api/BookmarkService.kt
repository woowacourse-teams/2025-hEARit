package com.onair.hearit.data.api

import com.onair.hearit.data.dto.BookmarkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BookmarkService {
    @GET("hearits/bookmarks")
    suspend fun getBookmarks(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
    ): Response<BookmarkResponse>
}
