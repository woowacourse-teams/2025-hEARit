package com.onair.hearit.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.exception.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.LoginReason
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.LikeRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val recentHearitRepository: RecentHearitRepository,
    private val getHearitUseCase: GetHearitUseCase,
    private val bookmarkRepository: BookmarkRepository,
    private val likeRepository: LikeRepository,
) : ViewModel() {
    private var hearitId: Long =
        savedStateHandle.get<Long>(HEARIT_ID_KEY) ?: -1L

    private val _hearit: MutableLiveData<Hearit?> = MutableLiveData()
    val hearit: LiveData<Hearit?> = _hearit

    private val _bookmarkId: MutableLiveData<Long?> = MutableLiveData()
    val bookmarkId: LiveData<Long?> = _bookmarkId

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _showLoginDialog = SingleLiveData<LoginReason>()
    val showLoginDialog: LiveData<LoginReason> = _showLoginDialog

    private val _highlightedId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val highlightedId: StateFlow<Long?> = _highlightedId.asStateFlow()

    init {
        if (hearitId > INVALID_HEARIT_ID) {
            fetchData()
        } else {
            Timber.w("PlayerDetailViewModel initialized with invalid hearitId: $hearitId")
            _toastMessage.value = R.string.player_detail_toast_hearit_load_fail
        }
    }

    fun toggleBookmark() {
        if (bookmarkId.value != null) {
            deleteBookmark()
        } else {
            addBookmark()
        }
    }

    fun toggleLike() {
        if (_isLiked.value) {
            deleteLike()
        } else {
            addLike()
        }
    }

    fun refreshData(newHearitId: Long) {
        if (hearitId == newHearitId) return
        _bookmarkId.value = null
        _hearit.value = null
        hearitId = newHearitId
        fetchData()
    }

    fun setHighlightedId(scriptId: Long?) {
        if (_highlightedId.value == scriptId) return
        _highlightedId.value = scriptId
    }

    private fun fetchData() {
        viewModelScope.launch {
            getHearitUseCase(hearitId)
                .onSuccess {
                    _hearit.value = it
                    _bookmarkId.value = it.bookmarkId
                    _isLiked.value = it.like.isLiked
                    _likeCount.value = it.like.count
                    saveRecentHearit()
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.player_detail_toast_hearit_load_fail
                }
        }
    }

    private fun addBookmark() {
        viewModelScope.launch {
            bookmarkRepository
                .addBookmark(hearitId)
                .onSuccess { bookmarkId ->
                    _bookmarkId.value = bookmarkId
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _showLoginDialog.value = LoginReason.BOOKMARK
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_add_bookmark_fail
                        }
                    }
                }
        }
    }

    private fun deleteBookmark() {
        val id = _bookmarkId.value ?: return
        viewModelScope.launch {
            bookmarkRepository
                .deleteBookmark(id)
                .onSuccess {
                    _bookmarkId.value = null
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.all_toast_delete_bookmark_fail
                }
        }
    }

    private fun addLike() {
        viewModelScope.launch {
            _isLiked.value = true
            _likeCount.value += 1

            likeRepository
                .addLike(hearitId)
                .onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _isLiked.value = false
                            _likeCount.value -= 1
                            _showLoginDialog.value = LoginReason.LIKE
                        }

                        else -> {
                            Timber.w(throwable)
                            _isLiked.value = false
                            _likeCount.value -= 1
                            _toastMessage.value = R.string.player_detail_toast_add_like_fail
                        }
                    }
                }
        }
    }

    private fun deleteLike() {
        viewModelScope.launch {
            _isLiked.value = false
            _likeCount.value -= 1

            likeRepository
                .deleteLike(hearitId)
                .onFailure { throwable ->
                    Timber.w(throwable)
                    _isLiked.value = true
                    _likeCount.value += 1
                    _toastMessage.value = R.string.player_detail_toast_delete_like_fail
                }
        }
    }

    private fun saveRecentHearit() {
        val hearit = hearit.value ?: return
        viewModelScope.launch {
            recentHearitRepository
                .saveRecentHearit(
                    RecentHearit(hearit.id, hearit.title),
                ).onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.player_detail_toast_recent_save_fail
                }
        }
    }

    companion object {
        private const val INVALID_HEARIT_ID: Long = -1L
    }
}
