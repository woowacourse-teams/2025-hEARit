package com.onair.hearit.presentation.setting.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.onair.hearit.R
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.component.AlarmContent
import com.onair.hearit.presentation.setting.component.SettingTopBar
import com.onair.hearit.presentation.theme.HearitBlack

private const val COMMUTE_NOTIFICATION_TOPIC: String = "commute_1900"

@Composable
fun AlarmScreen(
    viewModel: SettingViewModel,
    onBackClick: () -> Unit,
) {
    val context: Context = LocalContext.current
    val isPushNotificationEnabled: Boolean by viewModel.isPushNotificationEnabled.collectAsState()
    val shouldRequestPermission: Boolean by viewModel.shouldRequestNotificationPermission.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                viewModel.onPostNotificationPermissionResult(isGranted)
            },
        )

    LaunchedEffect(isPushNotificationEnabled) {
        if (isPushNotificationEnabled) {
            FirebaseMessaging.getInstance().subscribeToTopic(COMMUTE_NOTIFICATION_TOPIC)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(COMMUTE_NOTIFICATION_TOPIC)
        }
    }

    LaunchedEffect(shouldRequestPermission) {
        if (!shouldRequestPermission) return@LaunchedEffect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.onPostNotificationPermissionResult(true)
            return@LaunchedEffect
        }

        val permissionState: Int =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            )

        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            viewModel.onPostNotificationPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            SettingTopBar(
                title = stringResource(R.string.setting_alarm),
                onBackClick = onBackClick,
            )
        },
        containerColor = HearitBlack,
    ) { padding ->
        AlarmContent(
            modifier = Modifier.padding(padding),
            isPushNotificationEnabled = isPushNotificationEnabled,
            onPushNotificationToggleRequested = { newValue: Boolean ->
                viewModel.onPushNotificationToggleRequested(newValue)
            },
        )
    }
}
