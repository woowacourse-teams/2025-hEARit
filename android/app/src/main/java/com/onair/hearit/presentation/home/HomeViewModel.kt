package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val hearitRepository: HearitRepository,
    private val memberRepository: MemberRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val recommendationRepository: RecommendationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    // 로딩 중인 작업 개수 추적
    private val loadingJobs = mutableSetOf<String>()

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

    private fun startLoading(jobKey: String) {
        loadingJobs.add(jobKey)
        _uiState.update { it.copy(isLoading = true) }
    }

    private fun finishLoading(jobKey: String) {
        loadingJobs.remove(jobKey)
        if (loadingJobs.isEmpty()) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun fetchRecommendHearits() {
        viewModelScope.launch {
            safeLoad("recommend") {
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
            safeLoad("history") {
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
            safeLoad("recentUpload") {
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
            safeLoad("bookmark") {
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
            safeLoad("categories") {
                recommendationRepository
                    .getRecommendationCategories()
                    .onSuccess { categories ->
                        _uiState.update { it.copy(recommendationCategories = categories) }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.home_toast_grouped_category_load_fail
                    }
            }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            memberRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _uiState.update {
                        it.copy(userInfo = userInfo, isLoggedIn = true)
                    }
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _uiState.update {
                                it.copy(userInfo = UserInfo.default(), isLoggedIn = false)
                            }
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                            _uiState.update { it.copy(isLoggedIn = false) }
                        }
                    }
                }
        }
    }

    private suspend inline fun <T> safeLoad(
        jobKey: String,
        block: () -> T,
    ): T? {
        startLoading(jobKey)
        return try {
            block()
        } finally {
            withContext(NonCancellable) { finishLoading(jobKey) }
        }
    }
}
