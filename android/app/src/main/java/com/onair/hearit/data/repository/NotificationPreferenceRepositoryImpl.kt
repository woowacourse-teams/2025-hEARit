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

    override suspend fun getIsCommutePushEnabled(): Result<Boolean> =
        runCatching {
            // 1. 캐시 확인
            val cached: Boolean? = mutex.withLock { cachedIsCommutePushEnabled }
            if (cached != null) return@runCatching cached

            // 2. 로컬(DataStore) 조회
            val local: Boolean =
                notificationLocalDataSource.getIsCommutePushEnabled().getOrThrow()

            // 3. 캐시 갱신
            mutex.withLock { cachedIsCommutePushEnabled = local }
            local
        }.onFailure { throwable ->
            Timber.w(throwable, "❌ 푸시 알림 설정을 불러오는 데 실패했습니다.")
        }

    override suspend fun saveIsCommutePushEnabled(isEnabled: Boolean): Result<Unit> =
        runCatching {
            // 1. 캐시 먼저 갱신 (UI 반응 우선)
            mutex.withLock { cachedIsCommutePushEnabled = isEnabled }

            // 2. 로컬 저장
            notificationLocalDataSource
                .saveIsCommutePushEnabled(isEnabled)
                .getOrThrow()
        }.onFailure { throwable ->
            Timber.e(throwable, "❌ 푸시 알림 설정 저장에 실패했습니다. (메모리 캐시는 갱신됨)")
        }
}
