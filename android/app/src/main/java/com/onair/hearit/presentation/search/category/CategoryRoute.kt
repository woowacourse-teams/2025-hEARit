package com.onair.hearit.presentation.search.category

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CategoryRoute(
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collect { stringResId ->
            snackbarHostState.showSnackbar(context.getString(stringResId))
        }
    }

    CategoryScreen(
        categoryName = uiState.category?.name ?: "",
        categoryColor = uiState.category?.colorCode ?: "",
        hearits = uiState.hearits,
        onBack = onBack,
        onHearitClick = onHearitClick,
    )
}
