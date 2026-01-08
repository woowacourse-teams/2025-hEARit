package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.theme.HearitBlack
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val LOAD_MORE_THRESHOLD = 3

@Composable
fun SearchResult(
    hearits: List<SearchedHearit>,
    onHearitClick: (Long) -> Unit,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, hearits) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.filter { it != null }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= hearits.size - LOAD_MORE_THRESHOLD) {
                    onLoadNext()
                }
            }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack)
                .padding(top = 12.dp),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(
                items = hearits,
                key = { _, item -> item.id },
            ) { _, hearit ->
                SearchResultItem(
                    item = hearit,
                    onClick = { onHearitClick(hearit.id) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultScreenPreview() {
    val dummyHearits =
        listOf(
            SearchedHearit(
                id = 1L,
                title = "안드로이드 클린 아키텍처 이해하기",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
            SearchedHearit(
                id = 2L,
                title = "Compose로 화면 구성하기",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
            SearchedHearit(
                id = 3L,
                title = "Kotlin Flow 완전 정복",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
        )

    Box(modifier = Modifier.fillMaxSize()) {
        SearchResult(
            hearits = dummyHearits,
            onHearitClick = {},
            onLoadNext = {},
        )
    }
}
