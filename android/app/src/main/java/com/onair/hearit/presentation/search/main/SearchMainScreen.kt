package com.onair.hearit.presentation.search.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.search.main.component.CategoryGridList
import com.onair.hearit.presentation.search.main.component.SearchMainTopBar
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainScreen(
    categories: ImmutableList<Category>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchMainTopBar(
                scrollBehavior = scrollBehavior,
                onSearchBarClick = onSearchBarClick,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 48.dp),
            )
        },
        containerColor = HearitBlack,
    ) { paddingValues ->
        if (isLoading && categories.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = HearitPurple1)
            }
        } else {
            CategoryGridList(
                categories = categories,
                onCategoryClick = { category ->
                    onCategoryClick(category.id, category.name, category.colorCode)
                },
                modifier = Modifier.padding(paddingValues).padding(bottom = 24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SearchMainScreenPreview() {
    SearchMainScreen(
        categories =
            persistentListOf(
                Category(1L, "Kotlin", "#7C4DFF"),
                Category(2L, "Web", "#00BCD4"),
                Category(3L, "Database", "#FF5722"),
            ),
        isLoading = false,
        snackbarHostState = SnackbarHostState(),
        onSearchBarClick = {},
        onCategoryClick = { _, _, _ -> },
    )
}

@Preview
@Composable
private fun SearchMainScreenLoadingPreview() {
    SearchMainScreen(
        categories = persistentListOf(),
        isLoading = true,
        snackbarHostState = SnackbarHostState(),
        onSearchBarClick = {},
        onCategoryClick = { _, _, _ -> },
    )
}
