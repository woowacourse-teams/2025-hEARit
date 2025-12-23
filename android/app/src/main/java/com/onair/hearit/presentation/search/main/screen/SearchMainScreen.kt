package com.onair.hearit.presentation.search.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.search.main.component.CategoryGridList
import com.onair.hearit.presentation.search.main.component.SearchMainTopBar
import com.onair.hearit.presentation.theme.HearitBlack
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainScreen(
    categories: ImmutableList<Category>,
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
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
            onCategoryClick = onCategoryClick,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
