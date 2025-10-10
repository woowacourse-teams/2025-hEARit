package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.PlayingBookmarkHearit
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val recommendationRepository: RecommendationRepository,
) : ViewModel() {
    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _isLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _recommendHearits: MutableLiveData<List<RecommendHearit>> = MutableLiveData()
    val recommendHearits: LiveData<List<RecommendHearit>> = _recommendHearits

    private val _playingHistoryHearits: MutableLiveData<List<PlayingHistoryHearit>> =
        MutableLiveData()
    val playingHistoryHearits: LiveData<List<PlayingHistoryHearit>> = _playingHistoryHearits

    private val _recentUploadHearits: MutableLiveData<List<RecentUploadHearit>> = MutableLiveData()
    val recentUploadHearits: LiveData<List<RecentUploadHearit>> = _recentUploadHearits

    private val _playingBookmarkHearits: MutableLiveData<List<PlayingBookmarkHearit>> =
        MutableLiveData()
    val playingBookmarkHearits: LiveData<List<PlayingBookmarkHearit>> = _playingBookmarkHearits

    private val _recommendationCategories: MutableLiveData<List<RecommendationCategories>> =
        MutableLiveData()
    val recommendationCategories: LiveData<List<RecommendationCategories>> =
        _recommendationCategories

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
            val recommendDeferred = async { hearitRepository.getRecommendHearits() }
            val playingHistoryDeferred = async { playingHistoryRepository.getPlayingHistories() }
            val recentUploadDeferred = async { hearitRepository.getRecentUploadHearits() }
            val playingBookmarkDeferred = async { bookmarkRepository.getPlayingBookmarkHearits() }
            val groupedDeferred = async { recommendationRepository.getRecommendationCategories() }

            val recommendResult = recommendDeferred.await()
            val playingHistoryResult = playingHistoryDeferred.await()
            val recentUploadResult = recentUploadDeferred.await()
            val playingBookmarkResult = playingBookmarkDeferred.await()
            val groupedResult = groupedDeferred.await()

            recommendResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_recommend_load_fail
            }
            playingHistoryResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_playing_history_load_fail
            }
            recentUploadResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_recent_upload_load_fail
            }
            playingBookmarkResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_playing_bookmark_load_fail
            }
            groupedResult.onFailure { throwable ->
                Timber.w(throwable)
                _toastMessage.value = R.string.home_toast_grouped_category_load_fail
            }

            recommendResult.onSuccess { _recommendHearits.value = it }
            playingHistoryResult.onSuccess { _playingHistoryHearits.value = it }
            recentUploadResult.onSuccess { _recentUploadHearits.value = it }
            playingBookmarkResult.onSuccess { _playingBookmarkHearits.value = it }
            groupedResult.onSuccess { _recommendationCategories.value = it }

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
