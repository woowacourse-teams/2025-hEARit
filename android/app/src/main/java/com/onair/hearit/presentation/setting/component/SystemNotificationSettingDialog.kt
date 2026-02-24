package com.onair.hearit.presentation.setting.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.onair.hearit.R

@Composable
fun SystemNotificationSettingDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.setting_notification_system_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.setting_notification_system_dialog_description))
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(text = stringResource(R.string.setting_notification_system_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.setting_notification_system_dialog_dismiss))
            }
        },
    )
}
