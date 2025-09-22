package com.onair.hearit.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val authRepository: AuthRepository,
    private val preferencesLocalDataSource: PreferencesLocalDataSource,
    private val recentHearitRepository: RecentHearitRepository,
) : ViewModel() {
    private val _recentHearit = MutableLiveData<RecentHearit?>()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> = _isLoggingOut

    private val _withdrawState = MutableLiveData<Boolean>()
    val withdrawState: LiveData<Boolean> = _withdrawState

    val hearitUpdated = MutableLiveData<Unit>()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchRecentHearit()
    }

    fun updateLoginState(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    fun performLogout() {
        _isLoggingOut.value = true

        UserApiClient.instance.logout { error ->
            _isLoggingOut.value = false

            if (error != null) {
                Timber.w(error)
                _toastMessage.value = R.string.logout_fail
            } else {
                clearData()
                TokenInterceptorProvider.setAccessToken(null)
                _toastMessage.value = R.string.logout_success
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            authRepository
                .withdraw()
                .onSuccess {
                    UserApiClient.instance.unlink { error ->
                        if (error != null) {
                            Timber.w(error)
                            _toastMessage.value = R.string.withdraw_fail
                            _withdrawState.value = false
                            return@unlink
                        }

                        clearData()
                        _withdrawState.value = true
                        _toastMessage.value = R.string.withdraw_success
                    }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _withdrawState.value = false
                    _toastMessage.value = R.string.withdraw_fail
                }
        }
    }

    private fun fetchRecentHearit() {
        viewModelScope.launch {
            recentHearitRepository
                .getRecentHearit()
                .onSuccess { recent ->
                    _recentHearit.value = recent
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.main_toast_recent_load_fail
                }
        }
    }

    private fun clearData() {
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
