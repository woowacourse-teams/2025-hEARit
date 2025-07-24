package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.BookmarkRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository

class BookmarkRepositoryImpl(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>> =
        handleResult {
            val response = bookmarkDataSource.getBookmarks(page, size).getOrThrow()
            response.content.map { it.toDomain() }
        }

    override suspend fun addBookmark(hearitId: Long): Result<Long> =
        handleResult {
            val response = bookmarkDataSource.addBookmark(hearitId).getOrThrow()
            response.id
        }

    override suspend fun deleteBookmark(
        hearitId: Long,
        bookmarkId: Long,
    ): Result<Unit> =
        handleResult {
            bookmarkDataSource.deleteBookmark(hearitId, bookmarkId).getOrThrow()
        }
}
