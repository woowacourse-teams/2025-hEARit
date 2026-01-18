package com.onair.hearit.presentation.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.explore.component.ExploreShortsItem
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

@Composable
fun ExploreScreen(
    onNavigateToDetail: (id: Long) -> Unit,
    onPageChanged: (Int) -> Unit,
    currentPosition: Long,
    currentPageIndex: Int,
    duration: Long,
    onSpeedChanged: () -> Unit,
    onItemPlay: () -> Unit,
    shortsHearit: ImmutableList<ExploreHearit>,
    onPositionChanged: (Long) -> Unit,
    isPlaying: Boolean,
    speed: Float,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            pageCount = { shortsHearit.size },
        )

    LaunchedEffect(currentPageIndex) {
        if (pagerState.currentPage != currentPageIndex) {
            pagerState.animateScrollToPage(currentPageIndex)
        }
    }

    // 2. UI의 변경(사용자 스와이프)을 뷰모델에 반영
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        VerticalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize(),
            key = { index -> shortsHearit[index].id },
        ) { page ->
            val item = shortsHearit[page]

            ExploreShortsItem(
                item = item,
                currentPosition = currentPosition,
                duration = duration,
                onNavigateToDetail = onNavigateToDetail,
                onPositionChanged = onPositionChanged,
                onSpeedChanged = onSpeedChanged,
                onItemPlay = onItemPlay,
                isPlaying = isPlaying,
                speed = speed,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = AnnotatedString.fromHtml(stringResource(id = R.string.explore_hearit_guide)),
            style = HearitTypoGraphy.titleSmall,
            textAlign = TextAlign.Center,
            color = Gray4,
            modifier =
                Modifier
                    .statusBarsPadding() // <- 텍스트 박스만 상태바 아래로 밀리도록 여기에 추가
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .background(color = Gray1, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 20.dp)
                    .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun PreviewExploreScreen() {
    ExploreScreen(
        shortsHearit =
            immutableListOf(
                ExploreHearit(
                    id = 1,
                    title = "Activity를 쪼개며 배운 구조 설계",
                    categoryColorCode = "#000000",
                    isBookmarked = false,
                    bookmarkId = null,
                    keywords =
                        listOf(
                            Keyword(id = 1, name = "보안"),
                            Keyword(id = 1, name = "자동화"),
                            Keyword(id = 1, name = "협업"),
                        ),
                    cursorId = 1,
                    audioUrl = "",
                    script = listOf(),
                ),
            ),
        onNavigateToDetail = {},
        onPageChanged = {},
        currentPosition = 1000L,
        currentPageIndex = 0,
        duration = 10000L,
        onPositionChanged = {},
        onSpeedChanged = {},
        onItemPlay = {},
        isPlaying = false,
        speed = 1.0f,
    )
}
