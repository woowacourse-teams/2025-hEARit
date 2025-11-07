package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.MemberRepository

class UserRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : MemberRepository {
    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val localUser =
                userLocalDataSource
                    .getUserInfo()
                    .getOrThrow()
            if (localUser.id != -1L) return@runCatching localUser

            userRemoteDataSource
                .getUserInfo()
                .mapOrThrowDomain { it.toDomain() }
                .onSuccess { userLocalDataSource.saveUserInfo(it) }
                .getOrThrow()
        }
}
