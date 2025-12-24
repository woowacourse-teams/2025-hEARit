package com.onair.hearit.presentation.search.category

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel

@Composable
fun CategoryRoute(
    categoryId: Long,
    categoryName: String,
    categoryColor: String,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val viewModel: SearchViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val mainViewModel: MainViewModel = hiltViewModel(viewModelStoreOwner = activity)

    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()

    BackHandler(enabled = true) { onBack() }

    LaunchedEffect(categoryId) {
        viewModel.setCurrentCategory(categoryId, categoryName, categoryColor)
    }

    LaunchedEffect(categoryId) {
        mainViewModel.categoryUpdated.collect {
            viewModel.fetchCategoryHearits(isInitial = true)
        }
    }

    DisposableEffect(categoryId) {
        onDispose {
            viewModel.clearCategoryHearits()
        }
    }

    CategoryScreen(
        categoryName = categoryName,
        categoryColor = categoryColor,
        hearits = uiState.hearits,
        onBack = onBack,
        onHearitClick = onHearitClick,
    )
}
