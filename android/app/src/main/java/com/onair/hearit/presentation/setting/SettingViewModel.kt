package com.onair.hearit.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.BuildConfig
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SettingViewModel(
    private val dataStoreRepository: DataStoreRepository,
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
            dataStoreRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                }.onFailure {
                    _toastMessage.value = R.string.setting_toast_user_info_load_fail
                }
        }
    }
}
