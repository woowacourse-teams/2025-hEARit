package com.onair.hearit.presentation.search.main.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.main.component.CategoryGridList
import com.onair.hearit.presentation.search.main.component.SearchMainTopBar
import com.onair.hearit.presentation.theme.HearitBlack
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainScreen(
    viewModel: SearchViewModel,
    onSearchBarClick: () -> Unit,
    onCategoryClick: (Long, String, String) -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.observeAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.getCategories()
    }

    toastMessage?.let { resId ->
        LaunchedEffect(resId) {
            Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(HearitBlack)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchMainTopBar(
                scrollBehavior = scrollBehavior,
                onSearchBarClick = onSearchBarClick,
            )
        },
        containerColor = HearitBlack,
    ) { paddingValues ->
        CategoryGridList(
            categories = categories.toImmutableList(),
            onCategoryClick = { category ->
                AnalyticsProvider.get().logEvent(
                    AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
                    mapOf(AnalyticsParamKeys.CATEGORY_NAME to category.name),
                )
                onCategoryClick(category.id, category.name, category.colorCode)
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}
