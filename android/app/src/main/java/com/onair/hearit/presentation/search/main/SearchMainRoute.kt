package com.onair.hearit.presentation.search.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchMainRoute(
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
    viewModel: SearchMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.searchMainUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    SearchMainScreen(
        categories = uiState.categories,
        isLoading = uiState.isLoading,
        onSearchBarClick = onSearchBarClick,
        onCategoryClick = onCategoryClick,
    )
}
