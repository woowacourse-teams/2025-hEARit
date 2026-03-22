package com.onair.hearit.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.data.AuthEventManager
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.usecase.auth.SaveTokenUseCase
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val saveTokenUseCase: SaveTokenUseCase,
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
            saveTokenUseCase(accessToken, refreshToken)
                .onSuccess {
                    AuthEventManager.onLoginSuccess()
                    _loginState.value = true
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.login_toast_save_token_fail
                    _loginState.value = false
                }
        }
    }
}
