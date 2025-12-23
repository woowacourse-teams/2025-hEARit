package com.onair.hearit.presentation.search.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.main.screen.SearchMainScreen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SearchMainRoute(
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.observeAsState()
    val immutableCategories = remember(categories) { categories.toImmutableList() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getCategories()
    }

    toastMessage?.let { resId ->
        LaunchedEffect(resId) {
            Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    SearchMainScreen(
        categories = immutableCategories,
        onSearchBarClick = onSearchBarClick,
        onCategoryClick = { category ->
            onCategoryClick(
                category.id,
                category.name,
                category.colorCode,
            )
        },
        modifier = modifier,
    )
}
