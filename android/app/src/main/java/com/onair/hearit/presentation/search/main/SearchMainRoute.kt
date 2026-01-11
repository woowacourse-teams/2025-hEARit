package com.onair.hearit.presentation.search.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchMainRoute(
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
    viewModel: SearchMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.searchMainUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { stringResId ->
            snackbarHostState.showSnackbar(context.getString(stringResId))
        }
    }

    SearchMainScreen(
        categories = uiState.categories,
        isLoading = uiState.isLoading,
        snackbarHostState = snackbarHostState,
        onSearchBarClick = onSearchBarClick,
        onCategoryClick = onCategoryClick,
    )
}
