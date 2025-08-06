package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository

class BookmarkRepositoryImpl(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        token: String?,
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>> =
        bookmarkDataSource.getBookmarks(token, page, size).mapOrThrowDomain { bookmarkResponse ->
            bookmarkResponse.content.map { it.toDomain() }
        }

    override suspend fun addBookmark(
        token: String?,
        hearitId: Long,
    ): Result<Long> = bookmarkDataSource.addBookmark(token, hearitId).mapOrThrowDomain { it.id }

    override suspend fun deleteBookmark(
        token: String?,
        bookmarkId: Long,
    ): Result<Unit> = runCatching { bookmarkDataSource.deleteBookmark(token, bookmarkId) }
}
