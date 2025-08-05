package com.onair.hearit.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.BuildConfig
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.toBearerToken
import kotlinx.coroutines.launch

class SettingViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val memberRepository: MemberRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    val appVersion = BuildConfig.VERSION_NAME

    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            val token = dataStoreRepository.getAccessToken().getOrNull()
            if (token == null) {
                _toastMessage.value = R.string.setting_toast_user_info_load_fail
                return@launch
            }

            memberRepository
                .getUserInfo(token.toBearerToken())
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                }.onFailure {
                    crashlyticsLogger.recordException(it)
                    _toastMessage.value = R.string.setting_toast_user_info_load_fail
                }
        }
    }
}
