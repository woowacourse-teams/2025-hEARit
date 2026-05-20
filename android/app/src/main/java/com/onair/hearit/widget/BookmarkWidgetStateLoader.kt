package com.onair.hearit.widget

import com.onair.hearit.data.AuthHeaderProvider
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.domain.exception.DomainException
import com.onair.hearit.domain.repository.BookmarkRepository
import timber.log.Timber
import kotlin.math.max

class BookmarkWidgetStateLoader(
    private val bookmarkRepository: BookmarkRepository,
    private val authLocalDataSource: AuthLocalDataSource,
    private val authHeaderProvider: AuthHeaderProvider,
) {
    suspend fun loadBookmarks(): BookmarkWidgetUiState =
        try {
            syncWidgetAuthHeader()
            val result =
                bookmarkRepository.getBookmarks(
                    page = 0,
                    size = 10,
                    filter = "all",
                )
            val pageResult = result.getOrNull()
            val bookmarks =
                pageResult
                    ?.items
                    .orEmpty()
                    .map { bookmark ->
                        BookmarkWidgetItem(
                            hearitId = bookmark.hearitId,
                            title = bookmark.title,
                            category = bookmark.category.name.ifBlank { "기타" },
                        )
                    }
            val totalCount = max(pageResult?.paging?.totalElements ?: 0, bookmarks.size)
            when {
                result.isFailure -> errorState(result.exceptionOrNull())
                bookmarks.isEmpty() -> emptyState()
                else -> successState(totalCount = totalCount, bookmarks = bookmarks)
            }
        } catch (e: Exception) {
            Timber.e(e, "Widget loadBookmarks crashed")
            errorState(e)
        }

    private suspend fun syncWidgetAuthHeader() {
        try {
            val accessToken = authLocalDataSource.getAccessToken().getOrNull()
            authHeaderProvider.updateAccessToken(accessToken)
        } catch (e: Exception) {
            Timber.w(e, "Widget syncWidgetAuthHeader failed, continuing without auth")
        }
    }

    private fun errorState(throwable: Throwable?): BookmarkWidgetUiState {
        Timber.w(throwable, "Bookmark widget load failed")
        return when (throwable) {
            is DomainException.UserNotRegistered -> {
                BookmarkWidgetUiState(
                    status = BookmarkWidgetStatus.RequireLogin,
                )
            }

            is DomainException.NetworkConnection -> {
                BookmarkWidgetUiState(
                    status = BookmarkWidgetStatus.NetworkError,
                )
            }

            else -> {
                BookmarkWidgetUiState(
                    status = BookmarkWidgetStatus.UnknownError,
                )
            }
        }
    }

    private fun emptyState() =
        BookmarkWidgetUiState(
            status = BookmarkWidgetStatus.Empty,
        )

    private fun successState(
        totalCount: Int,
        bookmarks: List<BookmarkWidgetItem>,
    ) = BookmarkWidgetUiState(
        totalCount = totalCount,
        status = BookmarkWidgetStatus.Success,
        bookmarks = bookmarks,
    )
}
