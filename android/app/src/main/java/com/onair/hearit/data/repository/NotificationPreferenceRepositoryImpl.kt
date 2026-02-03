package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.NotificationLocalDataSource
import com.onair.hearit.domain.repository.NotificationPreferenceRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class NotificationPreferenceRepositoryImpl @Inject constructor(
    private val notificationLocalDataSource: NotificationLocalDataSource,
) : NotificationPreferenceRepository {
    @Volatile
    private var cachedIsCommutePushEnabled: Boolean? = null

    private val mutex: Mutex = Mutex()

    override fun getCachedCommutePushEnabled(): Boolean? = cachedIsCommutePushEnabled

    override suspend fun getCommutePushEnabled(): Result<Boolean> =
        runCatching {
            // 1. 캐시 확인
            val cached: Boolean? = mutex.withLock { cachedIsCommutePushEnabled }
            if (cached != null) return@runCatching cached

            // 2. 로컬(DataStore) 조회
            val local: Boolean =
                notificationLocalDataSource.getCommutePushEnabled().getOrThrow()

            // 3. 캐시 갱신
            mutex.withLock { cachedIsCommutePushEnabled = local }
            local
        }.onFailure { throwable ->
            val cached: Boolean? = mutex.withLock { cachedIsCommutePushEnabled }
            Timber.d("푸시 알림 설정 조회 실패: cachedIsCommutePushEnabled=$cached")
            Timber.w(throwable, "❌ 푸시 알림 설정을 불러오는 데 실패했습니다.")
        }

    override suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit> =
        runCatching {
            // 1. 로컬 저장
            notificationLocalDataSource
                .saveCommutePushEnabled(isEnabled)
                .getOrThrow()

            // 2. 저장 성공 후 캐시 갱신
            mutex.withLock { cachedIsCommutePushEnabled = isEnabled }
        }.onFailure { throwable ->
            val cached: Boolean? = mutex.withLock { cachedIsCommutePushEnabled }
            Timber.d("푸시 알림 설정 저장 실패: requestedIsEnabled=$isEnabled, cachedIsCommutePushEnabled=$cached")
            Timber.e(throwable, "❌ 푸시 알림 설정 저장에 실패했습니다.")
        }

    override suspend fun hasShownNotificationSuggestion(): Result<Boolean> =
        runCatching {
            notificationLocalDataSource
                .hasShownNotificationSuggestion()
                .getOrThrow()
        }.onFailure { throwable ->
            Timber.w(throwable, "❌ 알림 제안 노출 여부 조회 실패")
        }

    override suspend fun setNotificationSuggestionShown(): Result<Unit> =
        runCatching {
            notificationLocalDataSource
                .setNotificationSuggestionShown()
                .getOrThrow()
        }.onFailure { throwable ->
            Timber.e(throwable, "❌ 알림 제안 노출 여부 저장 실패")
        }
}
