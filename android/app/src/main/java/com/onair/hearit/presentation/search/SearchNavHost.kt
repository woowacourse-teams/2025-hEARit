package com.onair.hearit.presentation.search

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.presentation.search.category.CategoryRoute
import com.onair.hearit.presentation.search.detail.SearchDetailRoute
import com.onair.hearit.presentation.search.main.SearchMainRoute

@Composable
fun SearchNavHost(
    startArgs: SearchStartArgs = SearchStartArgs(),
    onExitSearch: () -> Unit,
    onHearitClick: (Long) -> Unit,
) {
    val navController = rememberNavController()
    val analyticsLogger = LocalAnalyticsLogger.current

    val startDestination: SearchRoute =
        startArgs.initialCategory ?: SearchRoute.SearchMain

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<SearchRoute.SearchMain> {
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
                onSearchBarClick = {
                    navController.navigate(SearchRoute.SearchDetail)
                },
                onCategoryClick = { id, name, colorCode ->
                    navController.navigate(
                        SearchRoute.Category(
                            id = id,
                            name = name,
                            colorCode = colorCode,
                        ),
                    )
                },
            )
        }

        composable<SearchRoute.SearchDetail> {
            SearchDetailRoute(
                onBackClick = { navController.navigateUp() },
                onHearitClick = onHearitClick,
            )
        }

        composable<SearchRoute.Category> {
            CategoryRoute(
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
