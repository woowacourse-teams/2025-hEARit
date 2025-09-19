package com.onair.hearit.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.domain.DomainException.NetworkConnection
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _checkToken: MutableLiveData<Boolean> = MutableLiveData()
    val checkToken: LiveData<Boolean> = _checkToken

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

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
                _checkToken.value = true
                authRepository.saveToken(accessToken)
                TokenInterceptorProvider.setAccessToken(accessToken)
            }.onFailure { throwable ->
                handleAccessTokenError(throwable, refreshToken)
            }
    }

    private fun handleAccessTokenError(
        throwable: Throwable,
        refreshToken: String,
    ) {
        when (throwable) {
            is NetworkConnection -> _toastMessage.value = R.string.splash_toast_network_check_fail
            is UserNotRegistered -> reissueAccessToken(refreshToken)
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
                    TokenInterceptorProvider.setAccessToken(newToken)
                }.onFailure { throwable ->
                    handleReissueError(throwable)
                }
        }
    }

    private fun handleReissueError(throwable: Throwable) {
        when (throwable) {
            is UserNotRegistered -> _checkToken.value = false
            else -> {
                Timber.w(throwable)
                _checkToken.value = false
                _toastMessage.value = R.string.splash_toast_refresh_token_fail
            }
        }
    }

    companion object {
        private const val DELAY_TIME = 1000L
    }
}
