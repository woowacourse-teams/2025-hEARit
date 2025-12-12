package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.exception.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import com.onair.hearit.domain.repository.UserRepository
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val hearitRepository: HearitRepository,
    private val userRepository: UserRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val recommendationRepository: RecommendationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchUserInfo()
        fetchData()
    }

    private fun fetchData() {
        fetchRecommendHearits()
        fetchPlayingHistory()
        fetchRecentUpload()
        fetchBookmarks()
        fetchCategories()
    }

    private fun startLoading(jobKey: HomeLoadKey) {
        _uiState.update { state ->
            state.copy(loadingKeys = state.loadingKeys + jobKey)
        }
    }

    private fun finishLoading(jobKey: HomeLoadKey) {
        _uiState.update { state ->
            state.copy(loadingKeys = state.loadingKeys - jobKey)
        }
    }

    private fun fetchRecommendHearits() {
        viewModelScope.launch {
            safeLoad(HomeLoadKey.RECOMMEND) {
                hearitRepository
                    .getRecommendHearits()
                    .onSuccess { hearits ->
                        _uiState.update { it.copy(recommendHearits = hearits) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_recommend_load_fail
                    }
            }
        }
    }

    private fun fetchPlayingHistory() {
        viewModelScope.launch {
            safeLoad(HomeLoadKey.PLAYING_HISTORY) {
                playingHistoryRepository
                    .getPlayingHistories()
                    .onSuccess { history ->
                        _uiState.update { it.copy(playingHistoryHearits = history) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_playing_history_load_fail
                    }
            }
        }
    }

    private fun fetchRecentUpload() {
        viewModelScope.launch {
            safeLoad(HomeLoadKey.RECENT_UPLOAD) {
                hearitRepository
                    .getRecentUploadHearits(size = 10)
                    .onSuccess { response ->
                        _uiState.update { it.copy(recentUploadHearits = response.items) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_recent_upload_load_fail
                    }
            }
        }
    }

    private fun fetchBookmarks() {
        viewModelScope.launch {
            safeLoad(HomeLoadKey.PLAYING_BOOKMARKS) {
                bookmarkRepository
                    .getBookmarks(
                        page = 0,
                        size = 10,
                        filter = "unfinished",
                    ).onSuccess { pageResult ->
                        _uiState.update { it.copy(playingBookmarkHearits = pageResult.items) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_playing_bookmark_load_fail
                    }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            safeLoad(HomeLoadKey.RECOMMENDATION_CATEGORIES) {
                recommendationRepository
                    .getRecommendationCategories()
                    .onSuccess { categories ->
                        _uiState.update { it.copy(recommendationCategories = categories) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_recommendation_category_load_fail
                    }
            }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            userRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _uiState.update {
                        it.copy(userInfo = userInfo)
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        when (throwable) {
                            is UserNotRegistered -> it.copy(userInfo = UserInfo.default())
                            else -> it // 일시적인 실패(네트워크, 서버 오류 등)에서는 이전 userInfo를 유지
                        }
                    }

                    if (throwable !is UserNotRegistered) {
                        Timber.w(throwable)
                        _toastMessage.value = R.string.all_toast_user_info_load_fail
                    }
                }
        }
    }

    private suspend inline fun safeLoad(
        jobKey: HomeLoadKey,
        block: () -> Unit,
    ) {
        startLoading(jobKey)
        try {
            block()
        } finally {
            withContext(NonCancellable) { finishLoading(jobKey) }
        }
    }
}
