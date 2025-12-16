package com.onair.hearit.presentation.splash

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.domain.exception.DomainException.NetworkConnection
import com.onair.hearit.domain.exception.DomainException.UserNotRegistered
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {
    private val _checkToken: MutableLiveData<Boolean> = MutableLiveData()
    val checkToken: LiveData<Boolean> = _checkToken

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _navigateToMain = SingleLiveData<Long?>()
    val navigateToMain: LiveData<Long?> = _navigateToMain

    fun checkValidAccessTokenWithDelay() {
        viewModelScope.launch {
            delay(DELAY_TIME)
            val tokens = authRepository.getTokens().getOrNull()
            if (tokens == null) {
                _checkToken.value = false
                return@launch
            }

            val (accessToken, refreshToken) = tokens
            validateAccessToken(accessToken, refreshToken)
        }
    }

    private suspend fun validateAccessToken(
        accessToken: String,
        refreshToken: String,
    ) {
        authRepository
            .checkAccessToken(accessToken)
            .onSuccess {
                val saved = authRepository.saveAccessToken(accessToken).isSuccess
                if (!saved) {
                    Timber.w("accessToken 저장에 실패했습니다.")
                }
                _checkToken.value = true
            }.onFailure { throwable ->
                handleAccessTokenError(throwable, refreshToken)
            }
    }

    private fun handleAccessTokenError(
        throwable: Throwable,
        refreshToken: String,
    ) {
        when (throwable) {
            is NetworkConnection -> {
                _toastMessage.value = R.string.splash_toast_network_check_fail
            }

            is UserNotRegistered -> {
                reissueAccessToken(refreshToken)
            }

            else -> {
                Timber.w(throwable)
                _checkToken.value = false
                _toastMessage.value = R.string.splash_toast_token_check_fail
            }
        }
    }

    private fun reissueAccessToken(refreshToken: String) {
        viewModelScope.launch {
            authRepository
                .reissue(refreshToken)
                .onSuccess { newToken ->
                    _checkToken.value = true
                }.onFailure { throwable ->
                    handleReissueError(throwable)
                }
        }
    }

    private fun handleReissueError(throwable: Throwable) {
        when (throwable) {
            is UserNotRegistered -> {
                _checkToken.value = false
            }

            else -> {
                Timber.w(throwable)
                _checkToken.value = false
                _toastMessage.value = R.string.splash_toast_refresh_token_fail
            }
        }
    }

    fun handleDeeplink(uri: Uri?) {
        val id =
            if (uri?.host == KAKAO_LINK_HOST) {
                uri.getQueryParameter("id")?.toLongOrNull()
            } else {
                null
            }

        if (id != null) {
            _navigateToMain.value = id
            analyticsLogger.logEvent(AnalyticsEventNames.SHARE_EVENT)
        } else if (uri != null) {
            _navigateToMain.value = null
        } else {
            _navigateToMain.value = null
        }
    }

    companion object {
        private const val DELAY_TIME = 1000L
        private const val KAKAO_LINK_HOST = "kakaolink"
    }
}
