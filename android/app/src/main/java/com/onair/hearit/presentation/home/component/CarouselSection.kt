package com.onair.hearit.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitPurple1
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun CarouselSection(
    items: ImmutableList<RecommendHearit>,
    onItemClick: (RecommendHearit) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val pagerState =
        rememberPagerState(
            initialPage = items.size / 2,
            pageCount = { items.size },
        )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 60.dp),
            pageSpacing = (-60).dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(340.dp),
        ) { page ->
            CarouselCard(
                item = items[page],
                pageOffset = {
                    (pagerState.currentPage - page).toFloat() +
                        pagerState.currentPageOffsetFraction
                },
                onClick = { onItemClick(items[page]) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PagerIndicator(
            pageCount = items.size,
            currentPage = pagerState.currentPage,
            onPageClick = { page ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page)
                }
            },
        )
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .background(
                            color = if (currentPage == index) HearitPurple1 else Gray2,
                            shape = CircleShape,
                        ).clickable { onPageClick(index) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CarouselSectionPreview() {
    MaterialTheme {
        CarouselSection(
            items =
                persistentListOf(
                    RecommendHearit(
                        id = 1L,
                        title = "합격 자기소개서 분석: 몰입 경험으로 개발 역량을 증명하는 법",
                        categoryName = "우테코",
                        categoryColor = "#12C6B0",
                    ),
                    RecommendHearit(
                        id = 2L,
                        title = "Kotlin Coroutines 완벽 가이드",
                        categoryName = "Kotlin",
                        categoryColor = "#7C4DFF",
                    ),
                    RecommendHearit(
                        id = 3L,
                        title = "Clean Architecture in Android",
                        categoryName = "Architecture",
                        categoryColor = "#00BCD4",
                    ),
                ),
            onItemClick = {},
        )
    }
}
