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

// XML 리소스 기반 색상 정의
private val HearitPurple3 = Color(0xFF9533F5)
private val HearitBlack1 = Color(0xFF272C32)

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
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
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
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    0.0f to HearitPurple3,
                    0.2f to HearitBlack1, // XML: centerY="0.2"
                    1.0f to HearitBlack1
                )
            )
    ) {
        // Profile Section
        LibraryProfileSection(
            userInfo = userInfo,
            onSettingClick = onSettingClick
        )

        // Header Section Margin (60dp from Profile)
        // XML에서는 Profile(iv_library_profile) 하단으로부터 60dp이나, 
        // ProfileSection 내부에 텍스트 높이 등이 포함되어 있으므로 적절히 조정 필요.
        // XML: tv_bookmarked_hearit_title(marginTop="60dp") app:layout_constraintTop_toBottomOf="@id/tv_library_nickname"
        Spacer(modifier = Modifier.height(60.dp))

        LibraryHeaderSection(
            totalCount = totalCount,
            isPlaying = isPlaying,
            onPlayAllClick = onPlayAllClick
        )

        // RecyclerView Margin (12dp from Total Count)
        // XML: fl_login_state(marginTop="12dp")
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp), // RecyclerView Padding
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 84.dp) // XML: RecyclerView(marginTop="20dp")
                ) {
                    items(
                        items = bookmarks,
                        key = { it.bookmarkId }
                    ) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onItemClick = onItemClick,
                            onOptionClick = onOptionClick
                        )
                    }
                }
            }
        }
    }
}
