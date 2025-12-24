package com.onair.hearit.presentation.search.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.presentation.search.category.component.CustomLinearProgressBar
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.toHashtagName
import com.onair.hearit.presentation.toTimeString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CategoryScreen(
    categoryName: String,
    categoryColor: String,
    hearits: ImmutableList<SearchedCategoryHearit>,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeColor =
        remember(categoryColor) {
            runCatching { Color(categoryColor.toColorInt()) }.getOrElse { HearitBlack }
        }

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
        CategoryTopBar(
            categoryName = categoryName,
            onBack = onBack,
        )

        CategoryHearitList(
            hearits = hearits,
            color = safeColor,
            onHearitClick = onHearitClick,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 152.dp, bottom = 60.dp),
        )
    }
}

@Composable
private fun CategoryTopBar(
    categoryName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(vertical = 12.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Gray4,
            )
        }

        Text(
            text = categoryName,
            modifier = Modifier.align(Alignment.Center),
            color = Gray4,
            style = HearitTypoGraphy.headlineMedium,
        )
    }
}

@Composable
private fun CategoryHearitList(
    hearits: ImmutableList<SearchedCategoryHearit>,
    color: Color,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = hearits,
            key = { it.id },
        ) { item ->
            SearchedHearitItem(
                item = item,
                color = color,
                onClick = { onHearitClick(item.id) },
            )
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
                )
                .clickable { onClick(item.id) }
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

@Preview(showBackground = true)
@Composable
private fun CategoryScreenPreview() {
    MaterialTheme {
        CategoryScreen(
            categoryName = "Android",
            categoryColor = "#73A01A",
            hearits =
                persistentListOf(
                    SearchedCategoryHearit(
                        id = 0,
                        title = "이건 첫 번째 레슨, 좋은 건 너만 알기",
                        playTime = 123,
                        lastPlayTime = 83782,
                        createdAt = "1234",
                        keywords = persistentListOf(Keyword(1, "aa"), Keyword(2, "bb")),
                        category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
                    ),
                    SearchedCategoryHearit(
                        id = 1,
                        title = "이제 두 번째 레슨, 슬픔도 너만 갖기",
                        playTime = 1234,
                        lastPlayTime = 192013,
                        createdAt = "1234",
                        keywords = persistentListOf(Keyword(1, "aa"), Keyword(2, "bb")),
                        category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
                    ),
                ),
            onBack = {},
            onHearitClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryScreenEmptyPreview() {
    MaterialTheme {
        CategoryScreen(
            categoryName = "Kotlin",
            categoryColor = "#7C4DFF",
            hearits = persistentListOf(),
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
            keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
        )

    MaterialTheme {
        SearchedHearitItem(dummy, Gray3, {})
    }
}
