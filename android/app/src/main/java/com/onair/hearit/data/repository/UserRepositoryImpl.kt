package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {
    @Volatile
    private var cachedUserInfo: UserInfo? = null
    private var cachedDeviceId: String? = null
    private val mutex = Mutex()

    override fun getCachedUserInfo(): UserInfo? = cachedUserInfo

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            // 1️⃣ 캐시 확인 (락)
            val cached = mutex.withLock { cachedUserInfo }
            if (cached != null) return@runCatching cached

            // 2️⃣ 로컬 시도 (락 없음)
            val local = userLocalDataSource.getUserInfo().getOrNull()
            if (local != null) {
                mutex.withLock { cachedUserInfo = local }
                return@runCatching local
            }

            // 3️⃣ 네트워크 호출 (락 없음)
            val remote =
                userRemoteDataSource
                    .getUserInfo()
                    .toDomainResult { it.toDomain() }
                    .getOrThrow()

            // 4️⃣ 캐시 업데이트 후 로컬 저장
            mutex.withLock { cachedUserInfo = remote }
            userLocalDataSource
                .saveUserInfo(remote)
                .onFailure {
                    Timber.e(it, "DataStore에 UserInfo 저장 실패 (메모리 캐시는 유지)")
                }

            remote
        }

    override suspend fun getOrCreateDeviceId(): Result<String> =
        runCatching {
            mutex.withLock {
                cachedDeviceId?.let { return@runCatching it }

                val deviceId =
                    userLocalDataSource
                        .getDeviceId()
                        .getOrElse {
                            val newId = UUID.randomUUID().toString()
                            userLocalDataSource.saveDeviceId(newId).getOrThrow()
                            newId
                        }

                cachedDeviceId = deviceId
                deviceId
            }
        }

    override suspend fun clearUserData(): Result<Unit> =
        runCatching {
            mutex.withLock {
                cachedUserInfo = null
                cachedDeviceId = null
                userLocalDataSource.clearData().getOrThrow()
            }
        }
}
