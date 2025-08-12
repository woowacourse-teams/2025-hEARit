package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.presentation.toBearerToken

class BookmarkRepositoryImpl(
    private val bookmarkDataSource: BookmarkRemoteDataSource,
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
) : BookmarkRepository {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<List<Bookmark>> =
        bookmarkDataSource
            .getBookmarks(page, size)
            .mapOrThrowDomain { bookmarkResponse ->
                bookmarkResponse.content.map { it.toDomain() }
            }

    override suspend fun addBookmark(hearitId: Long): Result<Long> {
        val accessToken = preferencesLocalDataSource.getAccessToken().getOrNull()

        return bookmarkDataSource
            .addBookmark(accessToken.toBearerToken(), hearitId)
            .mapOrThrowDomain { it.id }
    }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> {
        val accessToken = preferencesLocalDataSource.getAccessToken().getOrNull()
        return bookmarkDataSource
            .deleteBookmark(accessToken.toBearerToken(), bookmarkId)
            .mapOrThrowDomain { }
    }
}
