package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.di.TokenProvider

class MemberRemoteDataSourceImpl(
    private val memberService: MemberService,
    private val errorResponseHandler: ErrorResponseHandler,
) : MemberRemoteDataSource {
    override suspend fun getUserInfo(): Result<NetworkResult<UserInfoResponse>> =

        handleApiCall(
            apiCall = { memberService.getUserInfo(getAuthHeader()) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(
                    ERROR_RESPONSE_BODY_NULL_MESSAGE,
                )
            },
            errorHandler = errorResponseHandler,
        )

    private fun getAuthHeader(): String? {
        val token = TokenProvider.accessToken
        return if (token.isNullOrBlank()) {
            null
        } else {
            TOKEN.format(token)
        }
    }

    companion object {
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
        private const val TOKEN = "Bearer %s"
    }
}
