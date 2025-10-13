package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult

interface BookmarkRepository {
    suspend fun getBookmarks(
        page: Int?,
        size: Int?,
        filter: String,
    ): Result<PageResult<Bookmark>>

    suspend fun addBookmark(hearitId: Long): Result<Long>

    suspend fun deleteBookmark(bookmarkId: Long): Result<Unit>

    suspend fun getNextBookmark(currentId: Long): Result<Bookmark?>
}
