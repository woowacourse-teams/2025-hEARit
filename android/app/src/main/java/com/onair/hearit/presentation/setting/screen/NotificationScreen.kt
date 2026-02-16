package com.onair.hearit.presentation.setting.screen

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.messaging.FirebaseMessaging
import com.onair.hearit.R
import com.onair.hearit.notification.canNotify
import com.onair.hearit.notification.hasPostNotificationPermission
import com.onair.hearit.notification.isNotificationBlocked
import com.onair.hearit.notification.openNotificationSettings
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.component.NotificationContent
import com.onair.hearit.presentation.setting.component.SettingTopBar
import com.onair.hearit.presentation.setting.component.SystemNotificationSettingDialog
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitBlack
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

private const val COMMUTE_NOTIFICATION_TOPIC: String = "commute_1900"

@Composable
fun NotificationScreen(
    viewModel: SettingViewModel,
    onBackClick: () -> Unit,
) {
    val context: Context = LocalContext.current
    val isNotificationEnabled: Boolean by viewModel.isNotificationEnabled.collectAsState()
    val shouldRequestPermission: Boolean by viewModel.shouldRequestNotification.collectAsState()

    var showSystemDialog: Boolean by remember { mutableStateOf(false) }
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                viewModel.onPostNotificationPermissionResult(isGranted)
            },
        )

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { resId ->
            snackbarHostState.showSnackbar(message = context.getString(resId))
        }
    }

    SyncSystemNotificationEffect(
        context = context,
        viewModel = viewModel,
    )

    TopicSubscriptionEffect(isPushNotificationEnabled = isNotificationEnabled)

    PermissionRequestEffect(
        context = context,
        shouldRequestPermission = shouldRequestPermission,
        permissionLauncher = permissionLauncher,
        viewModel = viewModel,
        onNeedOpenSettings = { showSystemDialog = true },
    )

    SystemNotificationDialog(
        shouldShow = showSystemDialog,
        onDismiss = { showSystemDialog = false },
        onGoToSettings = {
            showSystemDialog = false
            context.openNotificationSettings()
        },
    )

    Scaffold(
        topBar = {
            SettingTopBar(
                title = stringResource(R.string.setting_notification),
                onBackClick = onBackClick,
            )
        },
        containerColor = HearitBlack,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 64.dp),
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = Gray2,
                    contentColor = HearitBlack,
                )
            }
        },
    ) { padding ->
        NotificationContent(
            modifier = Modifier.padding(padding),
            isPushEnabled = isNotificationEnabled,
            onPushChange = viewModel::onPushNotificationToggleRequested,
        )
    }
}

@Composable
private fun SyncSystemNotificationEffect(
    context: Context,
    viewModel: SettingViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, context) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.onSystemNotificationBlocked(
                        isNotificationAvailable = context.canNotify(),
                    )
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun TopicSubscriptionEffect(isPushNotificationEnabled: Boolean) {
    LaunchedEffect(isPushNotificationEnabled) {
        val task =
            if (isPushNotificationEnabled) {
                FirebaseMessaging.getInstance().subscribeToTopic(COMMUTE_NOTIFICATION_TOPIC)
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(COMMUTE_NOTIFICATION_TOPIC)
            }

        task.addOnFailureListener { throwable ->
            Timber.w(
                throwable,
                "❌ 출퇴근 푸시 토픽 ${if (isPushNotificationEnabled) "구독" else "해지"}에 실패했습니다.",
            )
        }
    }
}

@Composable
private fun PermissionRequestEffect(
    context: Context,
    shouldRequestPermission: Boolean,
    permissionLauncher: ActivityResultLauncher<String>,
    viewModel: SettingViewModel,
    onNeedOpenSettings: () -> Unit,
) {
    LaunchedEffect(shouldRequestPermission) {
        if (!shouldRequestPermission) return@LaunchedEffect

        if (context.isNotificationBlocked()) {
            viewModel.onSystemNotificationBlocked(isNotificationAvailable = false)
            onNeedOpenSettings()
            return@LaunchedEffect
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.onPostNotificationPermissionResult(true)
            return@LaunchedEffect
        }

        if (context.hasPostNotificationPermission()) {
            viewModel.onPostNotificationPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun SystemNotificationDialog(
    shouldShow: Boolean,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    if (!shouldShow) return

    SystemNotificationSettingDialog(
        onDismiss = onDismiss,
        onGoToSettings = onGoToSettings,
    )
}
