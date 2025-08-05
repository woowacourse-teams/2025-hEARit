package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
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
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
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
        private const val TOKEN = "Bearer %s"
    }
}
