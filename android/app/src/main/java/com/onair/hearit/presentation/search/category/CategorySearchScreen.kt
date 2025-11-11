package com.onair.hearit.presentation.search.category

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.toHashtagName
import com.onair.hearit.presentation.toTimeString

@Composable
fun CategorySearchScreen(
    viewModel: SearchViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hearits by viewModel.categoryHearits.collectAsStateWithLifecycle()
    val category = viewModel.currentCategory

    BackHandler(enabled = true) { onBack() }

    LaunchedEffect(Unit) {
        viewModel.fetchResultData(isInitial = true)

        // 외부에서 카테고리 업데이트 신호가 올 때마다 데이터를 다시 가져옵니다.
        // collect는 코루틴이 취소될 때까지 계속 실행됩니다.
        mainViewModel.categoryUpdated.collect {
            viewModel.fetchResultData(isInitial = true)
        }
    }

    GradientBackgroundScreen(
        colorCode = category?.colorCode ?: "#000000",
        categoryName = category?.name ?: "카테고리",
        hearits = hearits,
        onBack = onBack,
        onHearitClick = onHearitClick,
        modifier = modifier,
    )
}

@Composable
fun GradientBackgroundScreen(
    colorCode: String,
    categoryName: String,
    hearits: List<SearchedCategoryHearit>,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeColor = runCatching { Color(colorCode.toColorInt()) }.getOrElse { HearitBlack }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to safeColor,
                                    0.2f to HearitBlack,
                                ),
                        ),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Gray4,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 20.dp, start = 24.dp),
        ) {
            Text(
                text = categoryName,
                color = Gray4,
                style = HearitTypoGraphy.headlineMedium,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(R.drawable.ic_down),
                contentDescription = "categoryList",
                modifier =
                    Modifier
                        .size(20.dp)
                        .alpha(0f),
                tint = Gray4,
            )
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 152.dp, bottom = 60.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = hearits, key = { it.id }) { item ->
                SearchedHearitItem(
                    item = item,
                    color = safeColor,
                    onClick = onHearitClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun SearchedHearitItem(
    item: SearchedCategoryHearit,
    color: Color,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Gray1,
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onClick(item.id) }
                .padding(vertical = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
        ) {
            Text(
                text = item.title,
                modifier =
                    Modifier
                        .padding(start = 20.dp, end = 8.dp),
                color = Gray4,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = HearitTypoGraphy.bodyLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp, start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.keywords.forEach { keyword ->
                    Text(
                        text = keyword.toHashtagName(),
                        color = Gray2,
                        style = HearitTypoGraphy.labelMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = item.playTime.toTimeString(),
                    color = Gray4,
                    style = HearitTypoGraphy.labelMedium,
                )
            }

            val lastPlayTimeSec = (item.lastPlayTime ?: 0L) / 1000f
            val ratio = lastPlayTimeSec / item.playTime.toFloat()
            CustomLinearProgressBar(
                progress = ratio,
                backgroundColor = DarkGray,
                progressColor = color,
                cornerRadius = 48.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(start = 20.dp, end = 8.dp),
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .padding(end = 8.dp),
            tint = Gray4,
        )
    }
}

@Composable
fun CustomLinearProgressBar(
    progress: Float,
    backgroundColor: Color,
    progressColor: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .background(progressColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GradientBackgroundScreenPreview() {
    val dummyHearits =
        listOf(
            SearchedCategoryHearit(
                0,
                "이건 첫 번째 레슨, 좋은 건 너만 알기",
                playTime = 123,
                lastPlayTime = 83782,
                createdAt = "1234",
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
                category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
            ),
            SearchedCategoryHearit(
                1,
                "이제 두 번째 레슨, 슬픔도 너만 갖기",
                playTime = 1234,
                lastPlayTime = 192013,
                createdAt = "1234",
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
                category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
            ),
            SearchedCategoryHearit(
                2,
                "드디어 세 번째 레슨, 일희일비 않기",
                playTime = 1234,
                lastPlayTime = 99999,
                createdAt = "1234",
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
                category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
            ),
        )

    MaterialTheme {
        GradientBackgroundScreen(
            colorCode = "#73A01A",
            categoryName = "Android",
            hearits = dummyHearits,
            onBack = {},
            onHearitClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SearchedHearitItemPreview() {
    val dummy =
        SearchedCategoryHearit(
            0,
            "드디어 세 번째 레슨, 일희일비 않기. 좀 더 강해져야 돼. 웃어 넘길 수 있게...",
            playTime = 350,
            lastPlayTime = 99999,
            createdAt = "1234",
            keywords = listOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
        )

    MaterialTheme {
        SearchedHearitItem(dummy, Gray3, {})
    }
}
