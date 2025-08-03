package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.MemberRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.MemberRepository

class MemberRepositoryImpl(
    private val memberRemoteDataSource: MemberRemoteDataSource,
) : MemberRepository {
    override suspend fun getUserInfo(): Result<UserInfo> =
        handleResult {
            val response = memberRemoteDataSource.getUserInfo().getOrThrow()
            response.toDomain()
        }
}
