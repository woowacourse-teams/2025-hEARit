package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class UserRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {
    private var cachedUserInfo: UserInfo? = null
    private var cachedDeviceId: String? = null
    private val mutex = Mutex()

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

            // 4️⃣ 로컬 저장 후 캐시 업데이트
            userLocalDataSource.saveUserInfo(remote).getOrThrow()
            mutex.withLock { cachedUserInfo = remote }

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
