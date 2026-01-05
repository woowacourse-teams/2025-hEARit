package com.onair.hearit.presentation.search.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.presentation.search.category.component.SearchedHearitItem
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.rememberSafeColor
import com.onair.hearit.presentation.util.rememberTopFadeGradient
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
    fadeStop: Float = 0.2f,
) {
    val safeColor =
        rememberSafeColor(
            colorString = categoryColor,
            fallback = HearitBlack,
        )
    val gradientBrush =
        rememberTopFadeGradient(
            topColor = safeColor,
            bottomColor = HearitBlack,
        )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(brush = gradientBrush),
    ) {
        val gradientEndPadding = maxHeight * fadeStop

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
                    .padding(top = gradientEndPadding, bottom = 60.dp),
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
