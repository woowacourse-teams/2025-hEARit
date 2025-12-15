package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.BookmarkRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
        filter: String,
    ): Result<PageResult<Bookmark>> = bookmarkDataSource.getBookmarks(page, size, filter).mapOrThrowDomain { it.toDomain() }

    override suspend fun addBookmark(hearitId: Long): Result<Long> = bookmarkDataSource.addBookmark(hearitId).mapOrThrowDomain { it.id }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> = bookmarkDataSource.deleteBookmark(bookmarkId).mapOrThrowDomain { }

    override suspend fun getNextBookmark(currentId: Long): Result<Bookmark?> =
        getBookmarks(page = null, size = null, filter = "all").map { pageResult ->
            val sortedBookmarks = pageResult.items.sortedBy { it.bookmarkId }

            val currentIndex = sortedBookmarks.indexOfFirst { it.bookmarkId == currentId }
            if (currentIndex > 0) {
                sortedBookmarks[currentIndex - 1]
            } else {
                null
            }
        }
}
