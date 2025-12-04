package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.UserInfoResponse

class UserRemoteDataSourceImpl(
    private val memberService: MemberService,
    private val errorResponseHandler: ErrorResponseHandler,
) : UserRemoteDataSource {
    override suspend fun getUserInfo(): NetworkResult<UserInfoResponse> =
        handleApiCall(
            apiCall = { memberService.getUserInfo() },
            errorHandler = errorResponseHandler,
        )
}
