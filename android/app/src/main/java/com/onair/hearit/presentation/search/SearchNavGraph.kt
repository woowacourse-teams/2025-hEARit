package com.onair.hearit.presentation.search

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.category.CategoryScreen
import com.onair.hearit.presentation.search.main.screen.SearchMainScreen
import com.onair.hearit.presentation.search.recent.screen.SearchDetailScreen

// Routes
const val SEARCH_MAIN_ROUTE = "search_main"
const val SEARCH_DETAIL_ROUTE = "search_detail"
const val CATEGORY_ROUTE = "category/{categoryId}/{categoryName}/{categoryColor}"

// Navigation arguments
object CategoryArgs {
    const val CATEGORY_ID = "categoryId"
    const val CATEGORY_NAME = "categoryName"
    const val CATEGORY_COLOR = "categoryColor"
}

@Composable
fun SearchNavHost(
    navController: NavHostController,
    analyticsLogger: AnalyticsLogger,
    searchViewModel: SearchViewModel,
    mainViewModel: MainViewModel,
    onHearitClick: (Long) -> Unit,
    onCategoryBack: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = SEARCH_MAIN_ROUTE,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        // 검색 메인 화면
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

            SearchMainScreen(
                viewModel = searchViewModel,
                onSearchBarClick = {
                    navController.navigate(SEARCH_DETAIL_ROUTE)
                },
                onCategoryClick = { id, name, colorCode ->
                    analyticsLogger.logEvent(
                        AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
                        mapOf(AnalyticsParamKeys.CATEGORY_NAME to name),
                    )
                    val encodedColor = colorCode.removePrefix("#")
                    navController.navigate("category/$id/$name/$encodedColor")
                },
            )
        }

        composable(SEARCH_DETAIL_ROUTE) {
            SearchDetailScreen(
                viewModel = searchViewModel,
                onBackClick = {
                    navController.navigateUp()
                },
            )
        }

        composable(
            route = CATEGORY_ROUTE,
            arguments =
                listOf(
                    navArgument(CategoryArgs.CATEGORY_ID) { type = NavType.LongType },
                    navArgument(CategoryArgs.CATEGORY_NAME) { type = NavType.StringType },
                    navArgument(CategoryArgs.CATEGORY_COLOR) { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong(CategoryArgs.CATEGORY_ID) ?: 0L
            val categoryName = backStackEntry.arguments?.getString(CategoryArgs.CATEGORY_NAME) ?: ""
            val categoryColorRaw =
                backStackEntry.arguments?.getString(CategoryArgs.CATEGORY_COLOR) ?: ""

            val categoryColor =
                if (categoryColorRaw.startsWith("#")) {
                    categoryColorRaw
                } else {
                    "#$categoryColorRaw"
                }

            CategoryScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                categoryColor = categoryColor,
                viewModel = searchViewModel,
                mainViewModel = mainViewModel,
                onBack = onCategoryBack,
                onHearitClick = onHearitClick,
            )
        }
    }
}
