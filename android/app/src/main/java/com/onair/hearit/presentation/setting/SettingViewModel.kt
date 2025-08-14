package com.onair.hearit.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.BuildConfig
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingViewModel(
    private val memberRepository: MemberRepository,
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
            memberRepository
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
