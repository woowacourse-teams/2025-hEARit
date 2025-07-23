package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.BookmarkResponse

interface BookmarkRemoteDataSource {
    suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<BookmarkResponse>
}
