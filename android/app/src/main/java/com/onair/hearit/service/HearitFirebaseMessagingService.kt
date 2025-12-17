package com.onair.hearit.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onair.hearit.R
import com.onair.hearit.notification.HearitNotificationChannels
import com.onair.hearit.presentation.main.MainActivity
import timber.log.Timber

class HearitFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title: String =
            message.notification?.title
                ?: message.data[TITLE_KEY]
                ?: getString(R.string.app_name)

        val body: String =
            message.notification?.body
                ?: message.data[BODY_KEY]
                ?: ""

        showHomeNotification(
            title = title,
            body = body,
        )
    }

    private fun showHomeNotification(
        title: String,
        body: String,
    ) {
        if (!canPostNotification()) return

        HearitNotificationChannels.ensureCommutePushChannel(this)

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                HOME_PENDING_INTENT_COMMUTE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, HearitNotificationChannels.COMMUTE_PUSH_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mini_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_COMMUTE, notification)
        } catch (securityException: SecurityException) {
            Timber.Forest.w(securityException)
        }
    }

    private fun canPostNotification(): Boolean {
        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(this)

        // 앱 알림이 OS에서 차단된 상태면 중단
        if (!notificationManagerCompat.areNotificationsEnabled()) return false

        // Android 13+ 런타임 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState: Int =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            return permissionState == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    companion object {
        private const val NOTIFICATION_ID_COMMUTE: Int = 1900

        private const val HOME_PENDING_INTENT_COMMUTE: Int = 1900

        private const val TITLE_KEY: String = "title"
        private const val BODY_KEY: String = "body"
    }
}
