package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.category.screen.CategorySearchScreen

@Composable
fun CategorySearchRoute(
    viewModel: SearchViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onHearitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hearits by viewModel.categoryHearits.collectAsStateWithLifecycle()
    val category = viewModel.currentCategory

    LaunchedEffect(Unit) {
        viewModel.fetchResultData(isInitial = true)

        mainViewModel.categoryUpdated.collect {
            viewModel.fetchResultData(isInitial = true)
        }
    }

    CategorySearchScreen(
        colorCode = category?.colorCode ?: "#000000",
        categoryName = category?.name ?: "카테고리",
        hearits = hearits,
        onBack = onBack,
        onHearitClick = onHearitClick,
        modifier = modifier,
    )
}
