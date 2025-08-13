package com.onair.hearit.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.domain.DomainException
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
) : ViewModel() {
    private val _checkToken: MutableLiveData<Boolean> = MutableLiveData()
    val checkToken: LiveData<Boolean> = _checkToken

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    fun checkValidAccessTokenWithDelay() {
        viewModelScope.launch {
            delay(DELAY_TIME)
            checkValidAccessToken()
        }
    }

    private fun checkValidAccessToken() {
        viewModelScope.launch {
            val accessToken = preferencesLocalDataSource.getAccessToken().getOrNull()
            val refreshToken = preferencesLocalDataSource.getRefreshToken().getOrNull()
            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                _checkToken.value = false
                return@launch
            }

            val result = authRepository.checkAccessToken(accessToken)
            result
                .onSuccess {
                    _checkToken.value = true
                    TokenInterceptorProvider.setAccessToken(accessToken)
                }.onFailure { throwable ->
                    when (throwable) {
                        is DomainException.NetworkConnection -> {
                            _toastMessage.value = R.string.splash_toast_network_check_fail
                        }

                        is DomainException.UserNotRegistered -> {
                            refreshAccessToken(refreshToken)
                        }

                        else -> {
                            Timber.w(throwable)
                            _checkToken.value = false
                            _toastMessage.value = R.string.splash_toast_token_check_fail
                        }
                    }
                }
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        viewModelScope.launch {
            authRepository
                .reissue(refreshToken)
                .onSuccess { newToken ->
                    preferencesLocalDataSource.saveAccessToken(newToken)
                    _checkToken.value = true
                }.onFailure { throwable ->
                    when (throwable) {
                        is DomainException.UserNotRegistered -> {
                            _checkToken.value = false
                        }

                        else -> {
                            Timber.w(throwable)
                            _checkToken.value = false
                            _toastMessage.value = R.string.splash_toast_refresh_token_fail
                        }
                    }
                }
        }
    }

    companion object {
        private const val DELAY_TIME = 1000L
    }
}
