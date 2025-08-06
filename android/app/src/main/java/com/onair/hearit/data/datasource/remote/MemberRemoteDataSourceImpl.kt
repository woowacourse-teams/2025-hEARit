package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.UserInfoResponse

class MemberRemoteDataSourceImpl(
    private val memberService: MemberService,
    private val errorResponseHandler: ErrorResponseHandler,
) : MemberRemoteDataSource {
    override suspend fun getUserInfo(token: String?): Result<NetworkResult<UserInfoResponse>> =
        handleApiCall(
            apiCall = { memberService.getUserInfo(token) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
