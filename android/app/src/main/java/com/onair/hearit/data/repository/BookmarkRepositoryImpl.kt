package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.BookmarkRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkRemoteDataSource: BookmarkRemoteDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
        filter: String,
    ): Result<PageResult<Bookmark>> = bookmarkRemoteDataSource.getBookmarks(page, size, filter).toDomainResult { it.toDomain() }

    override suspend fun addBookmark(hearitId: Long): Result<Long> = bookmarkRemoteDataSource.addBookmark(hearitId).toDomainResult { it.id }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> =
        bookmarkRemoteDataSource.deleteBookmark(bookmarkId).toDomainResult()

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
