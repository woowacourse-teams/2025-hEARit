package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Bookmark

interface BookmarkRepository {
    suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>>

    suspend fun addBookmark(hearitId: Long): Result<Unit>

    suspend fun deleteBookmark(
        hearitId: Long,
        bookmarkId: Long,
    ): Result<Unit>
}
