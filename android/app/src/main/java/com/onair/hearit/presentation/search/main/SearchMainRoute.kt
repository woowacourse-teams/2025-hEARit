package com.onair.hearit.presentation.search.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.main.screen.SearchMainScreen

@Composable
fun SearchMainRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
) {
    val uiState by viewModel.searchMainUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    SearchMainScreen(
        categories = uiState.categories,
        isLoading = uiState.isLoading,
        onSearchBarClick = onSearchBarClick,
        onCategoryClick = onCategoryClick,
    )
}
