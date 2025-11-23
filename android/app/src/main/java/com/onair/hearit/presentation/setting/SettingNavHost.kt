package com.onair.hearit.presentation.setting

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.onair.hearit.presentation.setting.screen.ProfileScreen
import com.onair.hearit.presentation.setting.screen.SettingScreen

@Composable
fun SettingNavHost(
    navController: NavHostController,
    viewModel: SettingViewModel,
    onExitSetting: () -> Unit,
) {
    NavHost(
        navController,
        startDestination = "setting",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable("setting") {
            SettingScreen(
                viewModel = viewModel,
                onBackClick = onExitSetting,
                onProfileClick = { navController.navigate("profile") },
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
