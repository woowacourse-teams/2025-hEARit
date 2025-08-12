package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _isLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

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
        viewModelScope.launch {
            hearitRepository
                .getRecommendHearits()
                .onSuccess {
                    _recommendHearits.value = it
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.home_toast_recommend_load_fail
                }
        }

        viewModelScope.launch {
            hearitRepository
                .getCategoryHearits()
                .onSuccess { groupedCategory ->
                    _groupedCategory.value = groupedCategory
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.home_toast_grouped_category_load_fail
                }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            memberRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                    _isLoggedIn.value = true
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            _userInfo.value = UserInfo.default()
                            _isLoggedIn.value = false
                        }

                        else -> {
                            _isLoggedIn.value = false
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }
}
