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
    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val localUser =
                userLocalDataSource
                    .getUserInfo()
                    .getOrThrow()

            // 로컬 default(-1)면 remote만 쓰고 local은 반환하지 않음
            if (localUser.id == -1L) {
                return@runCatching userRemoteDataSource
                    .getUserInfo()
                    .mapOrThrowDomain { it.toDomain() }
                    .onSuccess { userLocalDataSource.saveUserInfo(it).getOrThrow() }
                    .getOrThrow()
            }

            // 로컬 이미 정상 → 바로 반환
            return@runCatching localUser
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
            userLocalDataSource.clearData().getOrThrow()
        }
}
