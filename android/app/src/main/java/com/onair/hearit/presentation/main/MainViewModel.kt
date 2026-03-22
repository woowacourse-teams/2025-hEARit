package com.onair.hearit.presentation.main

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.NotificationPreferenceRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.auth.LogoutUseCase
import com.onair.hearit.domain.usecase.auth.WithdrawUseCase
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.splash.SplashActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val recentHearitRepository: RecentHearitRepository,
    private val logoutUseCase: LogoutUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
) : ViewModel() {
    private val _recentHearit = MutableLiveData<RecentHearit?>()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> = _isLoggingOut

    private val _withdrawState = MutableLiveData<Boolean>()
    val withdrawState: LiveData<Boolean> = _withdrawState

    private val _categoryUpdated = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val categoryUpdated: SharedFlow<Unit> = _categoryUpdated

    private val _toastMessage = SingleLiveData<Int>()

    val toastMessage: LiveData<Int> = _toastMessage
    private val _navigateToDetail = SingleLiveData<Long>()
    val navigateToDetail: LiveData<Long> = _navigateToDetail

    private val _showNotificationSuggestion = SingleLiveData<Unit>()
    val showNotificationSuggestion: LiveData<Unit> = _showNotificationSuggestion

    val hearitUpdated = MutableLiveData<Unit>()

    init {
        fetchRecentHearit()
    }

    fun notifyCategoryUpdated() {
        _categoryUpdated.tryEmit(Unit)
    }

    fun updateLoginState(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true

            logoutUseCase()
                .onSuccess {
                    _toastMessage.value = R.string.logout_success
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.logout_fail
                }

            _isLoggingOut.value = false
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            withdrawUseCase()
                .onSuccess {
                    _withdrawState.value = true
                    _toastMessage.value = R.string.withdraw_success
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _withdrawState.value = false
                    _toastMessage.value = R.string.withdraw_fail
                }
        }
    }

    fun handleDeepLinkIntent(intent: Intent) {
        val fromDeeplink =
            intent.getBooleanExtra(SplashActivity.OPEN_DETAIL_FROM_DEEPLINK, false)
        val id = intent.getLongExtra(HEARIT_ID_KEY, -1L)

        if (fromDeeplink && id > -1L) {
            _navigateToDetail.value = id
        }
    }

    fun checkNotificationSuggestion(isSystemNotificationEnabled: Boolean) {
        if (isSystemNotificationEnabled) return

        viewModelScope.launch {
            val hasShown =
                notificationPreferenceRepository
                    .hasShownNotificationSuggestion()
                    .getOrDefault(false)

            if (!hasShown) _showNotificationSuggestion.call()
        }
    }

    fun onNotificationSuggestionShown() {
        viewModelScope.launch {
            notificationPreferenceRepository.setNotificationSuggestionShown()
        }
    }

    fun onNotificationAgreed() {
        viewModelScope.launch {
            notificationPreferenceRepository.saveCommutePushEnabled(true)
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
}
