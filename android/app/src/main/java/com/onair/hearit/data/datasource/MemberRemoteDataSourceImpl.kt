package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.di.TokenProvider
import com.onair.hearit.domain.UserNotRegisteredException

class MemberRemoteDataSourceImpl(
    private val memberService: MemberService,
) : MemberRemoteDataSource {
    override suspend fun getUserInfo(): Result<UserInfoResponse> =
        runCatching {
            val response = memberService.getUserInfo(getAuthHeader())
            when (response.code()) {
                200 ->
                    response.body() ?: throw IllegalStateException(
                        ERROR_RESPONSE_BODY_NULL_MESSAGE,
                    )

                401 -> throw UserNotRegisteredException(ERROR_SIGN_IN_MESSAGE)
                else -> throw Exception("API 호출 실패: ${response.code()} ${response.message()}")
            }
        }

    private fun getAuthHeader(): String? {
        val token = TokenProvider.accessToken
        return if (token.isNullOrBlank()) {
            null
        } else {
            TOKEN.format(token)
        }
    }

    companion object {
        private const val ERROR_SIGN_IN_MESSAGE = "회원가입되지 않은 사용자입니다."
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
        private const val TOKEN = "Bearer %s"
    }
}
