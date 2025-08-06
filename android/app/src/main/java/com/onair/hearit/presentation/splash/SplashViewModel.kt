package com.onair.hearit.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _checkToken: MutableLiveData<Boolean> = MutableLiveData()
    val checkToken: LiveData<Boolean> = _checkToken

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    fun checkValidAccessToken() {
        viewModelScope.launch {
            val accessToken = dataStoreRepository.getAccessToken().getOrNull()
            val refreshToken = dataStoreRepository.getRefreshToken().getOrNull()
            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                _checkToken.value = false
                return@launch
            }

            val result = authRepository.checkAccessToken("Bearer $accessToken")
            result
                .onSuccess {
                    _checkToken.value = true
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            refreshAccessToken(refreshToken)
                        }

                        else -> {
                            crashlyticsLogger.recordException(throwable)
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
                    dataStoreRepository.saveAccessToken(newToken)
                    _checkToken.value = true
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            _checkToken.value = false
                        }

                        else -> {
                            crashlyticsLogger.recordException(throwable)
                            _checkToken.value = false
                            _toastMessage.value = R.string.splash_toast_refresh_token_fail
                        }
                    }
                }
        }
    }
}
