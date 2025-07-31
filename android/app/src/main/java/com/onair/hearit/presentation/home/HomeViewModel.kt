package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
    private val recentHearitRepository: RecentHearitRepository,
) : ViewModel() {
    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _recentHearit: MutableLiveData<RecentHearit?> = MutableLiveData()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _recommendHearits: MutableLiveData<List<RecommendHearit>> = MutableLiveData()
    val recommendHearits: LiveData<List<RecommendHearit>> = _recommendHearits

    private val _groupedCategory: MutableLiveData<List<GroupedCategory>> = MutableLiveData()
    val groupedCategory: LiveData<List<GroupedCategory>> = _groupedCategory

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchUserInfo()
//        getRecentHearit()
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            hearitRepository
                .getRecommendHearits()
                .onSuccess { recommendHearits ->
                    _recommendHearits.value = recommendHearits
                }.onFailure {
                    _toastMessage.value = R.string.home_toast_recommend_load_fail
                }
        }

        viewModelScope.launch {
            hearitRepository
                .getCategoryHearits()
                .onSuccess { groupedCategory ->
                    _groupedCategory.value = groupedCategory
                }.onFailure {
                    _toastMessage.value = R.string.home_toast_grouped_category_load_fail
                }
        }
    }

    fun getRecentHearit() {
        viewModelScope.launch {
            recentHearitRepository
                .getRecentHearit()
                .onSuccess { recentHearit ->
                    _recentHearit.value = recentHearit
                }.onFailure {
                    _toastMessage.value = R.string.home_toast_recent_load_fail
                }
        }
    }

    private fun checkUserLogin() {
        viewModelScope.launch {
            dataStoreRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    // DataStore에 저장된 유저정보가 있으면 바로 사용
                    _userInfo.value = userInfo
                }.onFailure {
                    // DataStore에 유저정보 없으면 서버에서 조회
                    fetchUserInfo()
                }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            memberRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    saveUserInfo(userInfo)
                    _userInfo.value = userInfo
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            // 등록되지 않은 유저인 경우 (정상 응답)
                            val defaultUserInfo =
                                UserInfo(
                                    id = -1,
                                    nickname = "hEARit",
                                    profileImage = "",
                                )
                            saveUserInfo(defaultUserInfo)
                        }

                        else -> {
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }

    private fun saveUserInfo(userInfo: UserInfo) {
        viewModelScope.launch {
            dataStoreRepository
                .saveUserInfo(userInfo)
                .onFailure {
                    _toastMessage.value = R.string.all_toast_save_user_info_fail
                }
        }
    }
}
