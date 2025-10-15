package com.onair.hearit.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class PlayerDetailViewModel(
    private var hearitId: Long,
    private val recentHearitRepository: RecentHearitRepository,
    private val getHearitUseCase: GetHearitUseCase,
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _hearit: MutableLiveData<Hearit?> = MutableLiveData()
    val hearit: LiveData<Hearit?> = _hearit

    private val _bookmarkId: MutableLiveData<Long?> = MutableLiveData()
    val bookmarkId: LiveData<Long?> = _bookmarkId

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _showLoginDialog = SingleLiveData<Unit>()
    val showLoginDialog: LiveData<Unit> = _showLoginDialog

    init {
        fetchData()
    }

    fun toggleBookmark() {
        if (bookmarkId.value != null) {
            deleteBookmark()
        } else {
            addBookmark()
        }
    }

    fun refreshData(newHearitId: Long) {
        if (hearitId == newHearitId) return
        _bookmarkId.value = null
        _hearit.value = null
        hearitId = newHearitId
        fetchData()
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

    private fun fetchData() {
        viewModelScope.launch {
            getHearitUseCase(hearitId)
                .onSuccess {
                    _hearit.value = it
                    _bookmarkId.value = it.bookmarkId
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
                        is UserNotRegistered -> _showLoginDialog.call()
                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_add_bookmark_fail
                        }
                    }
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
}
