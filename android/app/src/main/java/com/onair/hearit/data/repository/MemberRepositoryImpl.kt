package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.MemberRepository

class MemberRepositoryImpl(
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
    private val memberRemoteDataSource: MemberRemoteDataSource,
) : MemberRepository {
    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val localUser =
                preferencesLocalDataSource
                    .getUserInfo()
                    .getOrThrow()
            if (localUser.id != -1L) return@runCatching localUser

            memberRemoteDataSource
                .getUserInfo()
                .mapOrThrowDomain { it.toDomain() }
                .onSuccess { preferencesLocalDataSource.saveUserInfo(it) }
                .getOrThrow()
        }
}
