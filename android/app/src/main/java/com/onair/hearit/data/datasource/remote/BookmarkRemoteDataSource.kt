package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse

interface BookmarkRemoteDataSource {
    suspend fun getBookmarks(
        page: Int? = 0,
        size: Int? = 10,
        filter: String,
        sort: String? = "createdAt,desc",
    ): NetworkResult<BookmarkResponse>

    suspend fun addBookmark(hearitId: Long): NetworkResult<BookmarkIdResponse>

    suspend fun deleteBookmark(bookmarkId: Long): NetworkResult<Unit>
}
