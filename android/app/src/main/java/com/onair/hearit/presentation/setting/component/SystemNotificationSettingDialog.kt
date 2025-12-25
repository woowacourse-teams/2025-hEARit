package com.onair.hearit.presentation.setting.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SystemNotificationSettingDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림이 꺼져 있어요") },
        text = { Text("휴대폰 설정에서 hEARit 알림을 켜야 푸시를 받을 수 있어요.") },
        confirmButton = {
            TextButton(onClick = onGoToSettings) { Text("설정으로 이동") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}
