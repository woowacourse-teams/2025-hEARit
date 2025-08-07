package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Bookmark

interface BookmarkRepository {
    suspend fun getBookmarks(
        token: String?,
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>>

    suspend fun addBookmark(
        token: String?,
        hearitId: Long,
    ): Result<Long>

    suspend fun deleteBookmark(
        token: String?,
        bookmarkId: Long,
    ): Result<Unit>
}
