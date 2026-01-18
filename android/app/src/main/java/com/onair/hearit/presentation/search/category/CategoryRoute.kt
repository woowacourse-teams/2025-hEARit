package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CategoryRoute(
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()

    CategoryScreen(
        categoryName = uiState.category?.name ?: "",
        categoryColor = uiState.category?.colorCode ?: "",
        hearits = uiState.hearits,
        onBack = onBack,
        onHearitClick = onHearitClick,
    )
}
