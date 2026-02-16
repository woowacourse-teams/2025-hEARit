package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.NotificationLocalDataSource
import com.onair.hearit.domain.repository.NotificationPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject

class NotificationPreferenceRepositoryImpl @Inject constructor(
    private val notificationLocalDataSource: NotificationLocalDataSource,
) : NotificationPreferenceRepository {
    override fun observeCommutePushEnabled(): Flow<Boolean> =
        notificationLocalDataSource
            .observeCommutePushEnabled()
            .distinctUntilChanged()
            .catch { throwable: Throwable ->
                Timber.w(throwable, "❌ 푸시 알림 설정 observe 실패")
                emit(false)
            }

    override suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit> =
        runCatching {
            notificationLocalDataSource
                .saveCommutePushEnabled(isEnabled)
                .getOrThrow()
        }.onFailure { throwable: Throwable ->
            Timber.e(throwable, "❌ 푸시 알림 설정 저장에 실패했습니다.")
        }

    override suspend fun hasShownNotificationSuggestion(): Result<Boolean> =
        runCatching {
            notificationLocalDataSource
                .hasShownNotificationSuggestion()
                .getOrThrow()
        }.onFailure { throwable: Throwable ->
            Timber.w(throwable, "❌ 알림 제안 노출 여부 조회 실패")
        }

    override suspend fun setNotificationSuggestionShown(): Result<Unit> =
        runCatching {
            notificationLocalDataSource
                .setNotificationSuggestionShown()
                .getOrThrow()
        }.onFailure { throwable: Throwable ->
            Timber.e(throwable, "❌ 알림 제안 노출 여부 저장 실패")
        }
}
