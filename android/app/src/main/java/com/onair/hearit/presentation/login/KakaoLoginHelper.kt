package com.onair.hearit.presentation.login

import android.app.Activity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class KakaoLoginHelper(
    private val activity: Activity,
    private val onSuccess: (OAuthToken) -> Unit,
    private val onError: (Throwable?) -> Unit,
) {
    private var isKakaoTalkLogin = true

    fun startLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            isKakaoTalkLogin = true
            loginWithKakaoTalk()
        } else {
            isKakaoTalkLogin = false
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoTalk() {
        UserApiClient.instance.loginWithKakaoTalk(activity, callback = kakaoCallback())
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(
            activity,
            callback = kakaoCallback(),
        )
    }

    private fun kakaoCallback(): (OAuthToken?, Throwable?) -> Unit {
        return callback@{ token, error ->
            when {
                token != null -> {
                    onSuccess(token)
                }

                error != null -> {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return@callback
                    if (isKakaoTalkLogin) {
                        isKakaoTalkLogin = false
                        loginWithKakaoAccount()
                    } else {
                        onError(error)
                    }
                }

                else -> {
                    onError(null)
                }
            }
        }
    }
}
