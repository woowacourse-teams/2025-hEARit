package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.UserRepository
import java.util.UUID

class UserRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {
    private var cachedUserInfo: UserInfo? = null

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            // 1️⃣ 메모리 캐시 확인
            cachedUserInfo?.let { return@runCatching it }

            // 2️⃣ 로컬에서 시도
            val local =
                userLocalDataSource
                    .getUserInfo()
                    .getOrNull()

            if (local != null) {
                cachedUserInfo = local
                return@runCatching local
            }

            // 3️⃣ 원격에서 가져오기 (예외 발생 가능)
            val remote =
                userRemoteDataSource
                    .getUserInfo()
                    .mapOrThrowDomain { it.toDomain() }
                    .getOrThrow()

            // 원격 정상 응답 → 로컬 반영 + 캐시 갱신
            userLocalDataSource.saveUserInfo(remote)
            cachedUserInfo = remote

            remote
        }

    override suspend fun getOrCreateUserId(): Result<String> =
        userLocalDataSource
            .getUserId()
            .recoverCatching {
                // userId가 없으면 새로 생성
                val newId = UUID.randomUUID().toString()
                userLocalDataSource
                    .saveUserId(newId)
                    .getOrThrow()
                newId
            }

    override suspend fun clearUserData(): Result<Boolean> =
        runCatching {
            cachedUserInfo = null
            userLocalDataSource.clearData().getOrThrow()
        }
}
