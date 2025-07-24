package com.onair.hearit.data.api

import com.onair.hearit.data.dto.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface MemberService {
    @GET("members/me")
    suspend fun getUserInfo(): Response<UserInfoResponse>
}
