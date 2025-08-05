package com.onair.hearit.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> = _loginState

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    fun kakaoLogin(accessToken: String) {
        viewModelScope.launch {
            authRepository
                .kakaoLogin(accessToken)
                .onSuccess { appToken ->
                    saveToken(appToken.accessToken, appToken.refreshToken)
                }.onFailure {
                    _toastMessage.value = R.string.login_toast_kakao_login_fail
                    _loginState.value = false
                }
        }
    }

    private fun saveToken(
        accessToken: String,
        refreshToken: String,
    ) {
        viewModelScope.launch {
            val result =
                runCatching {
                    dataStoreRepository.saveAccessToken(accessToken).getOrThrow()
                    dataStoreRepository.saveRefreshToken(refreshToken).getOrThrow()
                }

            result
                .onSuccess {
                    _loginState.value = true
                }.onFailure {
                    _toastMessage.value = R.string.login_toast_save_token_fail
                    _loginState.value = false
                    crashlyticsLogger.recordException(it)
                }
        }
    }
}
