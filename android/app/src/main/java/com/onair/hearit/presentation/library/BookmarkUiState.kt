package com.onair.hearit.presentation.library

sealed class BookmarkUiState {
    data object LoggedIn : BookmarkUiState()

    data object NotLoggedIn : BookmarkUiState()

    data object NoBookmarks : BookmarkUiState()
}
