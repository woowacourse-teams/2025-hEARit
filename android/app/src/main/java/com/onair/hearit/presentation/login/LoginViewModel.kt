package com.onair.hearit.presentation.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository,
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
                    saveAccessToken(appToken)
                }.onFailure {
                    _toastMessage.value = R.string.login_toast_kakao_login_fail
                    _loginState.value = false
                }
        }
    }

    private fun saveAccessToken(appToken: String) {
        viewModelScope.launch {
            dataStoreRepository
                .saveAccessToken(appToken)
                .onSuccess {
                    _loginState.value = true
                    Log.d("meeple_log", "${dataStoreRepository.getAccessToken()}")
                }.onFailure {
                    _toastMessage.value = R.string.login_toast_save_token_fail
                    _loginState.value = false
                }
        }
    }
}
