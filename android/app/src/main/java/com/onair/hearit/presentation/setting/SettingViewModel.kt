package com.onair.hearit.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.BuildConfig
import com.onair.hearit.R
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

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
                        is UserNotRegisteredException -> {
                            // 등록되지 않은 유저인 경우 (정상 응답)
                            _userInfo.value =
                                UserInfo(
                                    id = -1,
                                    nickname = "hEARit",
                                    profileImage = "",
                                )
                        }

                        else -> {
                            _toastMessage.value = R.string.setting_toast_user_info_load_fail
                        }
                    }
                }
        }
    }
}
