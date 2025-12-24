package com.onair.hearit.presentation.search.category

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.category.screen.CategoryScreen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CategoryRoute(
    categoryId: Long,
    categoryName: String,
    categoryColor: String,
    viewModel: SearchViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
) {
    val hearits by viewModel.categoryHearits.collectAsStateWithLifecycle()

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
        hearits = hearits.toImmutableList(),
        onBack = onBack,
        onHearitClick = onHearitClick,
    )
}
