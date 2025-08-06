package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse

interface BookmarkRemoteDataSource {
    suspend fun getBookmarks(
        token: String?,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<BookmarkResponse>>

    suspend fun addBookmark(
        token: String?,
        hearitId: Long,
    ): Result<NetworkResult<BookmarkIdResponse>>

    suspend fun deleteBookmark(
        token: String?,
        bookmarkId: Long,
    ): Result<NetworkResult<Unit>>
}
