package com.onair.hearit.presentation.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
) {
    val categories by viewModel.categories.observeAsState(initial = emptyList())
    val toastMessage by viewModel.toastMessage.observeAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.getCategories()
    }

    toastMessage?.let { resId ->
        val context = LocalContext.current
        LaunchedEffect(resId) {
            Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(HearitBlack),
        topBar = {
            SearchMainTopBar(
                scrollBehavior = scrollBehavior,
                onSearchBarClick = onSearchBarClick,
            )
        },
        containerColor = HearitBlack,
    ) { paddingValues ->
        CategoryGridList(
            categories = categories,
            onCategoryClick = { category ->
                AnalyticsProvider.get().logEvent(
                    AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
                    mapOf(AnalyticsParamKeys.CATEGORY_NAME to category.name),
                )
                onCategoryClick(category.id, category.name, category.colorCode)
            },
            modifier =
                Modifier
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HearitBlack),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.menu_search),
                    color = Color.White,
                    modifier =
                        Modifier
                            .padding(start = 4.dp),
                    style = HearitTypoGraphy.headlineMedium,
                )
            },
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = HearitBlack,
                    scrolledContainerColor = HearitBlack,
                ),
        )

        SearchBarInput(
            onSearchBarClick = onSearchBarClick,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = stringResource(R.string.search_category_text),
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 12.dp),
            color = Color.White,
            style = HearitTypoGraphy.titleLarge,
        )
    }
}

@Composable
fun SearchBarInput(
    onSearchBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(HearitBlack)
                    .clickable(onClick = onSearchBarClick)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.search_keyword),
                modifier = Modifier.weight(1f),
                color = Gray2,
                style = HearitTypoGraphy.bodyMedium,
            )

            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Gray4,
                modifier = Modifier.size(24.dp),
            )
        }

        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            thickness = 1.dp,
            color = Gray4,
        )
    }
}

@Composable
fun CategoryGridList(
    categories: List<Category>,
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
        contentPadding =
            PaddingValues(
                top = 12.dp,
                bottom = 40.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            style = HearitTypoGraphy.titleLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun SearchMainTopBarPreview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    MaterialTheme {
        SearchMainTopBar(scrollBehavior, {})
    }
}

@Composable
@Preview(showBackground = true)
fun SearchBarPreview() {
    MaterialTheme {
        SearchBarInput({ })
    }
}

@Composable
@Preview(showBackground = true)
fun CategoryGridListPreview() {
    val dummyCategories =
        listOf(
            Category(id = 0L, name = "카테고리1", colorCode = "#73A01A"),
            Category(id = 1L, name = "카테고리2", colorCode = "#1883B5"),
            Category(id = 2L, name = "카테고리3", colorCode = "#B5A168"),
        )

    MaterialTheme {
        CategoryGridList(dummyCategories, {})
    }
}

@Composable
@Preview(showBackground = true)
fun CategoryItemPreview() {
    val dummyCategory =
        Category(id = 0L, name = "카테고리이름", colorCode = "#73A01A")

    MaterialTheme {
        CategoryGridItem(dummyCategory, {})
    }
}
