package com.onair.hearit.presentation.library

import com.onair.hearit.domain.model.Bookmark

sealed class BookmarkUiState {
    data object LoggedIn : BookmarkUiState()

    data object NotLoggedIn : BookmarkUiState()

    data object NoBookmarks : BookmarkUiState()

    data class BookmarksExist(
        val bookmarks: List<Bookmark>,
    ) : BookmarkUiState()
}
