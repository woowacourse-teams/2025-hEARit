package com.onair.hearit.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class PlayerDetailViewModel(
    private val hearitId: Long,
    private val getHearitUseCase: GetHearitUseCase,
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _hearit: MutableLiveData<Hearit> = MutableLiveData()
    val hearit: LiveData<Hearit> = _hearit

    private val _isBookmarked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData()
    }

    fun toggleBookmark(bookmarkId: Long) {
        if (isBookmarked.value == true) {
            deleteBookmark(bookmarkId)
        } else {
            addBookmark()
        }
    }

    private fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            bookmarkRepository
                .deleteBookmark(hearitId, bookmarkId)
                .onSuccess { _isBookmarked.value = false }
                .onFailure {
                    _toastMessage.value = R.string.all_toast_delete_bookmark_fail
                }
        }
    }

    fun addBookmark() {
        viewModelScope.launch {
            bookmarkRepository
                .addBookmark(hearitId)
                .onSuccess { _isBookmarked.value = true }
                .onFailure {
                    _toastMessage.value = R.string.all_toast_add_bookmark_fail
                }
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            getHearitUseCase(hearitId)
                .onSuccess {
                    _hearit.value = it
                    _isBookmarked.value = it.isBookmarked
                }.onFailure { _toastMessage.value = R.string.player_detail_toast_hearit_load_fail }
        }
    }
}
