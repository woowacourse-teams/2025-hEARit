package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.search.component.HearitItem
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun SearchResultScreen(
    hearits: ImmutableList<SearchedHearit>,
    onHearitClick: (Long) -> Unit,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadMoreThreshold: Int = 3,
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
                if (lastVisibleIndex != null && lastVisibleIndex >= hearits.size - loadMoreThreshold) {
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
        if (hearits.isEmpty() && !isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append("검색된 ")
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = HearitPurple1,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append("히어릿")
                            }
                            append("이 없어요!")
                        },
                    color = Gray4,
                    style = HearitTypoGraphy.bodyLarge,
                )
            }
        } else if (hearits.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 68.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = hearits,
                    key = { it.id },
                ) { hearit ->
                    HearitItem(
                        title = hearit.title,
                        keywords = hearit.keywords,
                        playTime = hearit.playTime,
                        lastPlayTime = hearit.lastPlayTime,
                        progressColor = HearitPurple1,
                        onClick = { onHearitClick(hearit.id) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultScreenPreview() {
    val dummyHearits =
        persistentListOf(
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
        SearchResultScreen(
            hearits = dummyHearits,
            onHearitClick = {},
            onLoadNext = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultEmptyPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        SearchResultScreen(
            hearits = persistentListOf(),
            onHearitClick = {},
            onLoadNext = {},
        )
    }
}
