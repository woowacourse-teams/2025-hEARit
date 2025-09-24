package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
) : ViewModel() {
    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _isLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _recommendHearits: MutableLiveData<List<RecommendHearit>> = MutableLiveData()
    val recommendHearits: LiveData<List<RecommendHearit>> = _recommendHearits

    private val _recentHearits: MutableLiveData<List<PlayingHistoryHearit>> = MutableLiveData()
    val recentHearits: LiveData<List<PlayingHistoryHearit>> = _recentHearits

    private val _groupedCategory: MutableLiveData<List<GroupedCategory>> = MutableLiveData()
    val groupedCategory: LiveData<List<GroupedCategory>> = _groupedCategory

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchUserInfo()
        fetchData()
    }

    private fun fetchData() {
        _isLoading.value = true

        viewModelScope.launch {
            val recentDeferred = async { playingHistoryRepository.getPlayingHistories() }
            val recommendDeferred = async { hearitRepository.getRecommendHearits() }
            val groupedDeferred = async { hearitRepository.getCategoryHearits() }

            val recentResult = recentDeferred.await()
            val recommendResult = recommendDeferred.await()
            val groupedResult = groupedDeferred.await()

            recentResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_recent_load_fail
            }
            recommendResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_recommend_load_fail
            }
            groupedResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_grouped_category_load_fail
            }

            recentResult.onSuccess { _recentHearits.value = it }
            recommendResult.onSuccess { _recommendHearits.value = it }
            groupedResult.onSuccess { _groupedCategory.value = it }

            _isLoading.value = false
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
                    _isLoggedIn.value = false

                    when (throwable) {
                        is UserNotRegistered -> _userInfo.value = UserInfo.default()

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }
}
