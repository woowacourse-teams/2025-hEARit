package com.onair.hearit.presentation.search

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.presentation.search.category.CategoryRoute
import com.onair.hearit.presentation.search.detail.SearchDetailScreen
import com.onair.hearit.presentation.search.main.SearchMainRoute

// Routes
const val SEARCH_MAIN_ROUTE = "search_main"
const val SEARCH_DETAIL_ROUTE = "search_detail"
const val CATEGORY_ROUTE = "category"

@Composable
fun SearchNavHost(
    startArgs: SearchStartArgs = SearchStartArgs(),
    onExitSearch: () -> Unit,
    onHearitClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val analyticsLogger = LocalAnalyticsLogger.current

    val startDestination =
        if (startArgs.initialCategory != null) {
            CATEGORY_ROUTE
        } else {
            SEARCH_MAIN_ROUTE
        }

    // Direct entry (홈에서 직접 진입)
    LaunchedEffect(startArgs.initialCategory) {
        startArgs.initialCategory?.let { category ->
            viewModel.setCurrentCategory(category)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(SEARCH_MAIN_ROUTE) {
            LaunchedEffect(Unit) {
                analyticsLogger.logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW,
                    mapOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to AnalyticsParamKeys.SCREEN_NAME_SEARCH,
                        FirebaseAnalytics.Param.SCREEN_CLASS to "SearchMainScreen",
                    ),
                )
            }

            SearchMainRoute(
                viewModel = viewModel,
                onSearchBarClick = {
                    navController.navigate(SEARCH_DETAIL_ROUTE)
                },
                onCategoryClick = { id, name, colorCode ->
                    viewModel.setCurrentCategory(CategoryNavModel(id, name, colorCode))
                    navController.navigate(CATEGORY_ROUTE)
                },
            )
        }

        composable(SEARCH_DETAIL_ROUTE) {
            SearchDetailScreen(
                onBackClick = {
                    navController.navigateUp()
                },
            )
        }

        composable(CATEGORY_ROUTE) {
            CategoryRoute(
                viewModel = viewModel,
                onBack = {
                    if (startArgs.isDirectEntry) {
                        onExitSearch()
                    } else {
                        navController.navigateUp()
                    }
                },
                onHearitClick = onHearitClick,
            )
        }
    }
}
