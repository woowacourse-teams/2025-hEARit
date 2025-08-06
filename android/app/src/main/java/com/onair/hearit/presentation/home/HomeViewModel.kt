package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.foldWithCrashlytics
import com.onair.hearit.presentation.launchWithLogging
import com.onair.hearit.presentation.toBearerToken
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _recommendHearits: MutableLiveData<List<RecommendHearit>> = MutableLiveData()
    val recommendHearits: LiveData<List<RecommendHearit>> = _recommendHearits

    private val _groupedCategory: MutableLiveData<List<GroupedCategory>> = MutableLiveData()
    val groupedCategory: LiveData<List<GroupedCategory>> = _groupedCategory

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchUserInfo()
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launchWithLogging(crashlyticsLogger) {
            hearitRepository
                .getRecommendHearits()
                .foldWithCrashlytics(
                    crashlyticsLogger,
                    onSuccess = { _recommendHearits.value = it },
                    onFailure = { _toastMessage.value = R.string.home_toast_recommend_load_fail },
                )
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

    private fun fetchUserInfo() {
        viewModelScope.launch {
            val token = dataStoreRepository.getAccessToken().getOrNull()
            if (token == null) {
                _userInfo.value = UserInfo.default()
                return@launch
            }

            memberRepository
                .getUserInfo(token.toBearerToken())
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            _userInfo.value = UserInfo.default()
                        }

                        else -> {
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }
}
