package com.onair.hearit.presentation.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.library.BookmarkUiState

@Composable
fun LibraryScreen(
    uiState: BookmarkUiState,
    userInfo: UserInfo,
    bookmarks: List<Bookmark>,
    totalCount: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    onSettingClick: () -> Unit,
    onLoginClick: () -> Unit,
    onPlayAllClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onOptionClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Infinite Scroll Logic
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            !isLoading && totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFF1A1A1A), // bg_gradient_vertical 시작 색상 유추
                                    Color(0xFF000000),
                                ),
                        ),
                ),
    ) {
        LibraryProfileSection(
            userInfo = userInfo,
            onSettingClick = onSettingClick,
        )

        LibraryHeaderSection(
            totalCount = totalCount,
            isPlaying = isPlaying,
            onPlayAllClick = onPlayAllClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (uiState) {
            is BookmarkUiState.NotLoggedIn -> {
                LibraryLoginRequiredView(onLoginClick = onLoginClick)
            }

            is BookmarkUiState.NoBookmarks -> {
                LibraryEmptyBookmarkView()
            }

            is BookmarkUiState.LoggedIn -> {
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                    contentPadding =
                        androidx.compose.foundation.layout
                            .PaddingValues(bottom = 84.dp),
                    // Bottom navigation height etc
                ) {
                    items(
                        items = bookmarks,
                        key = { it.bookmarkId },
                    ) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onItemClick = onItemClick,
                            onOptionClick = onOptionClick,
                        )
                    }
                }
            }
        }
    }
}
