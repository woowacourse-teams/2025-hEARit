package com.onair.hearit.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import com.onair.hearit.R
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(items.size) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) {
                        HearitPurple1
                    } else {
                        Gray2
                    }

                Box(
                    modifier =
                        Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .background(color, CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(iteration)
                                }
                            },
                )
            }
        }
    }
}

@Composable
private fun CarouselCard(
    item: RecommendHearit,
    pageOffset: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor =
        remember(item.categoryColor) {
            item.categoryColor.toComposeColor()
        }

    Card(
        modifier =
            modifier
                .zIndex(1f - pageOffset().absoluteValue)
                .graphicsLayer {
                    val offset = pageOffset()
                    val scale = lerp(0.88f, 1f, 1f - offset.absoluteValue.coerceIn(0f, 1f))
                    val alphaValue = lerp(0.6f, 1f, 1f - offset.absoluteValue.coerceIn(0f, 1f))
                    scaleX = scale
                    scaleY = scale
                    alpha = alphaValue
                }.clickable(
                    onClick = onClick,
                    onClickLabel = item.title,
                ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (pageOffset().absoluteValue < 0.5f) 8.dp else 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = item.categoryName,
                color = Gray4,
                textAlign = TextAlign.Start,
                style = HearitTypoGraphy.titleLarge,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_home_record),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f),
                )
            }

            Text(
                text = item.title,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                color = Gray4,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = HearitTypoGraphy.bodyLarge,
            )
        }
    }
}

private fun String.toComposeColor(): Color =
    try {
        val colorString = if (this.startsWith("#")) this else "#$this"
        Color(colorString.toColorInt())
    } catch (_: IllegalArgumentException) {
        Color(0xFF1A1A1A)
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

@Preview(showBackground = true, name = "Current Card (Center)")
@Composable
private fun CarouselCardPreview() {
    MaterialTheme {
        CarouselCard(
            item =
                RecommendHearit(
                    id = 1L,
                    title = "합격 자기소개서 분석: 몰입 경험으로 개발 역량을 증명하는 법",
                    categoryName = "우테코",
                    categoryColor = "#12C6B0",
                ),
            pageOffset = { 0f },
            onClick = {},
            modifier = Modifier.height(320.dp),
        )
    }
}

@Preview(showBackground = true, name = "Side Card")
@Composable
private fun CarouselCardSidePreview() {
    MaterialTheme {
        CarouselCard(
            item =
                RecommendHearit(
                    id = 2L,
                    title = "Kotlin Coroutines 완벽 가이드",
                    categoryName = "Kotlin",
                    categoryColor = "#7C4DFF",
                ),
            pageOffset = { 1f },
            onClick = {},
            modifier = Modifier.height(320.dp),
        )
    }
}
