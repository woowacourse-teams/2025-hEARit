package com.onair.hearit.data.mapper

import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.domain.model.LoginToken

fun KakaoLoginResponse.toDomain(): LoginToken =
    LoginToken(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
    )
