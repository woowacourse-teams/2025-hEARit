package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.search.SearchViewModel

@Composable
fun CategoryRoute(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
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
