package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.domain.UserNotRegisteredException

class MemberRemoteDataSourceImpl(
    private val memberService: MemberService,
) : MemberRemoteDataSource {
    override suspend fun getUserInfo(): Result<UserInfoResponse> =
        runCatching {
            val response = memberService.getUserInfo()
            when (response.code()) {
                200 -> response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
                401 -> throw UserNotRegisteredException("회원가입되지 않은 사용자입니다.")
                else -> throw Exception("API 호출 실패: ${response.code()} ${response.message()}")
            }
        }
}
