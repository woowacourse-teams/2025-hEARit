package com.onair.hearit.presentation.setting

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.onair.hearit.presentation.setting.SettingRoute.ALARM
import com.onair.hearit.presentation.setting.SettingRoute.PROFILE
import com.onair.hearit.presentation.setting.SettingRoute.SETTING
import com.onair.hearit.presentation.setting.screen.NotificationScreen
import com.onair.hearit.presentation.setting.screen.ProfileScreen
import com.onair.hearit.presentation.setting.screen.SettingScreen

@Composable
fun SettingNavHost(
    navController: NavHostController,
    viewModel: SettingViewModel,
    onExitSetting: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = SETTING,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(SETTING) {
            SettingScreen(
                viewModel = viewModel,
                onBackClick = onExitSetting,
                onProfileClick = { navController.navigate(PROFILE) },
                onAlarmClick = { navController.navigate(ALARM) },
                onLogin = onLogin,
                onLogout = onLogout,
                onWithdraw = onWithdraw,
            )
        }

        composable(PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(ALARM) {
            NotificationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

object SettingRoute {
    const val SETTING = "setting"
    const val PROFILE = "profile"
    const val ALARM = "alarm"
}
