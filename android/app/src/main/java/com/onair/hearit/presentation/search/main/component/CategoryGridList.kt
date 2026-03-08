package com.onair.hearit.presentation.search.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CategoryGridList(
    categories: ImmutableList<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack)
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = categories,
            key = { it.id },
        ) { category ->
            CategoryGridItem(
                category = category,
                onClick = { onCategoryClick(category) },
            )
        }
    }
}

@Composable
fun CategoryGridItem(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        remember(category.colorCode) {
            try {
                Color(category.colorCode.toColorInt())
            } catch (_: Exception) {
                Color.Gray
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.name,
            color = Gray4,
            style = HearitTypoGraphy.titleMedium,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun CategoryGridListPreview() {
    val categories =
        persistentListOf(
            Category(id = 0L, name = "카테고리1", colorCode = "#73A01A"),
            Category(id = 1L, name = "카테고리2", colorCode = "#1883B5"),
            Category(id = 2L, name = "카테고리3", colorCode = "#B5A168"),
        )

    MaterialTheme {
        CategoryGridList(
            categories = categories,
            onCategoryClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun CategoryItemPreview() {
    val dummyCategory =
        Category(id = 0L, name = "카테고리이름", colorCode = "#73A01A")

    MaterialTheme {
        CategoryGridItem(dummyCategory, {})
    }
}
