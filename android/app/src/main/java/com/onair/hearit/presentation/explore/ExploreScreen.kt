package com.onair.hearit.presentation.explore

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.explore.component.ExploreShortsItem
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.coroutines.delay

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
    showSwipeGuide: Boolean,
    onGuideFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            pageCount = { shortsHearit.size },
        )

    val pagerOffsetY = remember { Animatable(0f) }

    LaunchedEffect(showSwipeGuide) {
        if (showSwipeGuide) {
            delay(300)

            repeat(2) {
                pagerOffsetY.animateTo(
                    targetValue = -200f,
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                )

                pagerOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                )
                delay(400)
            }
        } else {
            pagerOffsetY.snapTo(0f)
        }
    }

    LaunchedEffect(currentPageIndex, shortsHearit.size) {
        val pageCount = shortsHearit.size
        if (pageCount == 0) return@LaunchedEffect

        val target = currentPageIndex.coerceIn(0, pageCount - 1)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                // 사용자가 드래그를 멈추거나 페이지가 완전히 바뀌었을 때만 호출됨
                if (page != currentPageIndex) {
                    onPageChanged(page)
                }
            }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        VerticalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = pagerOffsetY.value
                    },
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

        if (showSwipeGuide) {
            SwipeUpGuide(
                modifier = Modifier.align(Alignment.BottomCenter),
                onFinished = onGuideFinished,
            )
        }
    }
}

@Composable
private fun SwipeUpGuide(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.swipe_up))
    val repeatCount = 2

    LaunchedEffect(composition) {
        composition?.let { comp ->
            val totalDuration = comp.duration * repeatCount
            delay(totalDuration.toLong())
            onFinished()
        }
    }

    LottieAnimation(
        composition = composition,
        iterations = repeatCount,
        modifier =
            modifier
                .size(200.dp),
    )
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
        showSwipeGuide = true,
        onGuideFinished = {},
    )
}
