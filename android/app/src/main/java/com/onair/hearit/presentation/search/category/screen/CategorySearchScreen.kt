package com.onair.hearit.presentation.search.category.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.presentation.search.category.component.SearchedHearitItem
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CategorySearchScreen(
    colorCode: String,
    categoryName: String,
    hearits: ImmutableList<SearchedCategoryHearit>,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = true) { onBack() }

    GradientBackgroundScreen(
        colorCode = colorCode,
        categoryName = categoryName,
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
    hearits: ImmutableList<SearchedCategoryHearit>,
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

@Preview(showBackground = true)
@Composable
fun GradientBackgroundScreenPreview() {
    val dummyHearits =
        persistentListOf(
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
