package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse

interface BookmarkRemoteDataSource {
    suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<BookmarkResponse>

    suspend fun addBookmark(hearitId: Long): Result<BookmarkIdResponse>

    suspend fun deleteBookmark(bookmarkId: Long): Result<Unit>
}
