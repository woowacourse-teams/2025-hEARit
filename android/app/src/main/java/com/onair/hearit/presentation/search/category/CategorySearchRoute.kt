package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.category.screen.CategorySearchScreen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CategorySearchRoute(
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val categoryHearits by viewModel.categoryHearits.collectAsStateWithLifecycle()
    val category = viewModel.currentCategory

    LaunchedEffect(Unit) {
        viewModel.fetchResultData(isInitial = true)
    }

    LaunchedEffect(mainViewModel.categoryUpdated) {
        mainViewModel.categoryUpdated.collect {
            viewModel.fetchResultData(isInitial = true)
        }
    }

    CategorySearchScreen(
        colorCode = category.colorCode,
        categoryName = category.name,
        hearits = categoryHearits.toImmutableList(),
        onBack = onBack,
        onHearitClick = onHearitClick,
        modifier = modifier,
    )
}
