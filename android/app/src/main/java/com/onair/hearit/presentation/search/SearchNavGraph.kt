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

    // Direct entry인 경우 초기 카테고리 데이터 세팅
    LaunchedEffect(startArgs.initialCategory) {
        if (startArgs.initialCategory != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set(KEY_CATEGORY_NAV_MODEL, startArgs.initialCategory)
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
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        KEY_CATEGORY_NAV_MODEL,
                        CategoryNavModel(
                            id = id,
                            name = name,
                            colorCode = colorCode,
                        ),
                    )
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

        composable(CATEGORY_ROUTE) { backStackEntry ->
            val model =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<CategoryNavModel>(KEY_CATEGORY_NAV_MODEL)
                    // Direct entry인 경우 previousBackStackEntry가 없으므로 startArgs에서 가져옴
                    ?: startArgs.initialCategory

            if (model == null) {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
                return@composable
            }

            CategoryRoute(
                categoryId = model.id,
                categoryName = model.name,
                categoryColor = model.colorCode,
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
