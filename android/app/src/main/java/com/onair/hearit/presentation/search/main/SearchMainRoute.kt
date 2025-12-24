package com.onair.hearit.presentation.search.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.search.SearchViewModel

@Composable
fun SearchMainRoute(
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val viewModel: SearchViewModel = hiltViewModel(viewModelStoreOwner = activity)
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
