package com.onair.hearit.widget

data class BookmarkWidgetUiState(
    val totalCount: Int = 0,
    val status: BookmarkWidgetStatus = BookmarkWidgetStatus.Loading,
    val bookmarks: List<BookmarkWidgetItem> = emptyList(),
)

data class BookmarkWidgetItem(
    val hearitId: Long,
    val title: String,
    val category: String,
)

enum class BookmarkWidgetStatus {
    Loading,
    RequireLogin,
    NetworkError,
    UnknownError,
    Empty,
    Success,
}
