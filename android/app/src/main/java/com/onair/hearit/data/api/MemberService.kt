package com.onair.hearit.data.api

import com.onair.hearit.data.dto.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface MemberService {
    @GET("members/me")
    suspend fun getUserInfo(
        @Header("Authorization") token: String,
    ): Response<UserInfoResponse>
}
