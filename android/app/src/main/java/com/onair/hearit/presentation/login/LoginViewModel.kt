package com.onair.hearit.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.data.AuthEventManager
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
    private val authRepository: AuthRepository,
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
                }.onFailure { throwable ->
                    Timber.w(throwable)
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
                    preferencesLocalDataSource.saveAccessToken(accessToken).getOrThrow()
                    preferencesLocalDataSource.saveRefreshToken(refreshToken).getOrThrow()
                }.recoverCatching { throwable ->
                    // saveAccessToken이 성공했지만 saveRefreshToken이 실패하는 경우 롤백
                    preferencesLocalDataSource.clearData()
                    throw throwable
                }

            result
                .onSuccess {
                    TokenInterceptorProvider.setAccessToken(accessToken)
                    AuthEventManager.onLoginSuccess()
                    _loginState.value = true
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.login_toast_save_token_fail
                    _loginState.value = false
                }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            preferencesLocalDataSource
                .clearData()
                .onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.main_toast_clear_token_fail
                }
        }
    }
}
