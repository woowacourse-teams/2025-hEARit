package com.onair.hearit.data.repository

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.BookmarkRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository

class BookmarkRepositoryImpl(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
    private val crashlyticsLogger: CrashlyticsLogger,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>> =
        handleResult(crashlyticsLogger) {
            val response = bookmarkDataSource.getBookmarks(page, size).getOrThrow()
            response.content.map { it.toDomain() }
        }

    override suspend fun addBookmark(hearitId: Long): Result<Long> =
        handleResult(crashlyticsLogger) {
            val response = bookmarkDataSource.addBookmark(hearitId).getOrThrow()
            response.id
        }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> =
        handleResult(crashlyticsLogger) {
            bookmarkDataSource.deleteBookmark(bookmarkId).getOrThrow()
        }
}
