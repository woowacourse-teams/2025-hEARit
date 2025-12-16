package com.onair.hearit.presentation.search.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.main.screen.SearchMainScreen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SearchMainRoute(
    viewModel: SearchViewModel,
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.observeAsState()
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
        categories = categories.toImmutableList(),
        onSearchBarClick = onSearchBarClick,
        onCategoryClick = { category ->
            AnalyticsProvider.get().logEvent(
                AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
                mapOf(AnalyticsParamKeys.CATEGORY_NAME to category.name),
            )
            onCategoryClick(category.id, category.name, category.colorCode)
        },
        modifier = modifier,
    )
}
