package com.onair.hearit.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.BuildConfig
import com.onair.hearit.R
import com.onair.hearit.domain.exception.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.NotificationPreferenceRepository
import com.onair.hearit.domain.repository.UserRepository
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
) : ViewModel() {
    val appVersion = BuildConfig.VERSION_NAME

    private val _userInfo = MutableStateFlow(userRepository.getCachedUserInfo())
    val userInfo = _userInfo.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(false)
    val isNotificationEnabled = _isNotificationEnabled.asStateFlow()

    private val _shouldRequestNotification = MutableStateFlow(false)
    val shouldRequestNotification = _shouldRequestNotification.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _snackbarMessage = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    init {
        loadUserInfo()
        observeNotificationSetting()
    }

    fun onPushNotificationToggleRequested(isEnabled: Boolean) {
        if (!isEnabled) {
            applyToggleAndPersist(
                targetEnabled = false,
                successResId = R.string.setting_notification_push_disabled,
                failureResId = R.string.setting_notification_push_save_failed,
            )
            _shouldRequestNotification.value = false
            return
        }
        _shouldRequestNotification.value = true
    }

    fun onPostNotificationPermissionResult(isGranted: Boolean) {
        _shouldRequestNotification.value = false

        if (!isGranted) {
            applyToggleAndPersist(
                targetEnabled = false,
                successResId = R.string.setting_notification_push_disabled,
                failureResId = R.string.setting_notification_push_save_failed,
            )
            _toastMessage.value = R.string.all_toast_notification_permission_denied
            return
        }

        applyToggleAndPersist(
            targetEnabled = true,
            successResId = R.string.setting_notification_push_enabled,
            failureResId = R.string.setting_notification_push_save_failed,
        )
    }

    fun onSystemNotificationBlocked(isNotificationAvailable: Boolean) {
        if (isNotificationAvailable) return
        if (!_isNotificationEnabled.value && !_shouldRequestNotification.value) return

        _shouldRequestNotification.value = false

        applyToggleAndPersist(
            targetEnabled = false,
            successResId = R.string.setting_notification_push_blocked_by_system,
            failureResId = R.string.setting_notification_push_save_failed,
        )
    }

    private fun applyToggleAndPersist(
        targetEnabled: Boolean,
        successResId: Int,
        failureResId: Int,
    ) {
        val previousEnabled: Boolean = _isNotificationEnabled.value

        _isNotificationEnabled.value = targetEnabled

        viewModelScope.launch {
            notificationPreferenceRepository
                .saveCommutePushEnabled(targetEnabled)
                .onSuccess {
                    _snackbarMessage.tryEmit(successResId)
                }.onFailure { throwable ->
                    Timber.e(throwable)
                    _isNotificationEnabled.value = previousEnabled
                    _snackbarMessage.tryEmit(failureResId)
                }
        }
    }

    private fun loadUserInfo() {
        if (_userInfo.value != null) return
        fetchUserInfo()
    }

    private fun observeNotificationSetting() {
        viewModelScope.launch {
            notificationPreferenceRepository
                .observeCommutePushEnabled()
                .collect { isEnabled ->
                    _isNotificationEnabled.value = isEnabled
                }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            userRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _userInfo.value = UserInfo.default()
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }
}
