package com.onair.hearit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val recentHearitRepository: RecentHearitRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _recentHearit = MutableLiveData<RecentHearit?>()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> = _isLoggingOut

    private val _withdrawState = MutableLiveData<Boolean>()
    val withdrawState: LiveData<Boolean> = _withdrawState

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
                crashlyticsLogger.recordException(error)
                _toastMessage.value = R.string.logout_fail
            } else {
                clearAccessToken()
                _toastMessage.value = R.string.logout_success
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            val token = dataStoreRepository.getAccessToken().getOrNull()

            authRepository
                .withdraw(token?.toBearerToken())
                .onSuccess {
                    UserApiClient.instance.unlink { error ->
                        if (error != null) {
                            crashlyticsLogger.recordException(error)
                            _toastMessage.value = R.string.withdraw_fail
                            _withdrawState.value = false
                            return@unlink
                        }

                        clearAccessToken()
                        _withdrawState.value = true
                        _toastMessage.value = R.string.withdraw_success
                    }
                }.onFailure {
                    crashlyticsLogger.recordException(it)
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
                }.onFailure {
                    crashlyticsLogger.recordException(it)
                    _toastMessage.value = R.string.main_toast_recent_load_fail
                }
        }
    }

    private fun clearAccessToken() {
        viewModelScope.launch {
            dataStoreRepository
                .clearData()
                .onFailure {
                    crashlyticsLogger.recordException(it)
                    _toastMessage.value = R.string.main_toast_clear_token_fail
                }
        }
    }
}
