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
    ): Result<NetworkResult<BookmarkResponse>>

    suspend fun addBookmark(hearitId: Long): Result<NetworkResult<BookmarkIdResponse>>

    suspend fun deleteBookmark(bookmarkId: Long): Result<NetworkResult<Unit>>
}
