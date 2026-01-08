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

    private val _isPushNotificationEnabled = MutableStateFlow(false)
    val isPushNotificationEnabled = _isPushNotificationEnabled.asStateFlow()

    private val _shouldRequestNotificationPermission = MutableStateFlow(false)
    val shouldRequestNotificationPermission = _shouldRequestNotificationPermission.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _snackbarMessage = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    init {
        loadUserInfo()
        loadPushNotificationSetting()
    }

    fun onPushNotificationToggleRequested(isEnabled: Boolean) {
        if (!isEnabled) {
            _isPushNotificationEnabled.value = false
            _shouldRequestNotificationPermission.value = false
            persistPushNotificationSetting(false)

            _snackbarMessage.tryEmit(R.string.setting_alarm_push_disabled)
            return
        }
        _shouldRequestNotificationPermission.value = true
    }

    fun onPostNotificationPermissionResult(isGranted: Boolean) {
        _isPushNotificationEnabled.value = isGranted
        _shouldRequestNotificationPermission.value = false
        persistPushNotificationSetting(isGranted)

        if (isGranted) {
            _snackbarMessage.tryEmit(R.string.setting_alarm_push_enabled)
        } else {
            _snackbarMessage.tryEmit(R.string.setting_alarm_push_disabled)
            _toastMessage.value = R.string.all_toast_notification_permission_denied
        }
    }

    fun onSystemNotificationAvailabilityChecked(isNotificationAvailable: Boolean) {
        if (isNotificationAvailable) return
        if (!_isPushNotificationEnabled.value && !_shouldRequestNotificationPermission.value) return

        _isPushNotificationEnabled.value = false
        _shouldRequestNotificationPermission.value = false
        persistPushNotificationSetting(false)

        _snackbarMessage.tryEmit(R.string.setting_alarm_push_blocked_by_system)
    }

    private fun loadUserInfo() {
        if (_userInfo.value != null) return
        fetchUserInfo()
    }

    private fun loadPushNotificationSetting() {
        viewModelScope.launch {
            notificationPreferenceRepository
                .getIsCommutePushEnabled()
                .onSuccess { isEnabled ->
                    _isPushNotificationEnabled.value = isEnabled
                }.onFailure { throwable ->
                    Timber.w(throwable)
                }
        }
    }

    private fun persistPushNotificationSetting(isEnabled: Boolean) {
        viewModelScope.launch {
            notificationPreferenceRepository
                .saveIsCommutePushEnabled(isEnabled)
                .onFailure { throwable ->
                    Timber.e(throwable)
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
