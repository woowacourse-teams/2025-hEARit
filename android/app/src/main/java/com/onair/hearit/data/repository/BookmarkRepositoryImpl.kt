package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository

class BookmarkRepositoryImpl(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>> =
        bookmarkDataSource.getBookmarks(page, size).mapOrThrowDomain { bookmarkResponse ->
            bookmarkResponse.content.map { it.toDomain() }
        }

    override suspend fun addBookmark(hearitId: Long): Result<Long> = bookmarkDataSource.addBookmark(hearitId).mapOrThrowDomain { it.id }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> = runCatching { bookmarkDataSource.deleteBookmark(bookmarkId) }
}
