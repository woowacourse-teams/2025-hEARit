package com.onair.hearit.presentation.main.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class PlaylistViewModel(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _bookmarks: MutableLiveData<List<Bookmark>> = MutableLiveData()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchBookmarks()
    }

    private fun fetchBookmarks() {
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = null, size = null)
                .onSuccess { pageResult ->
                    _bookmarks.value = pageResult.items
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.library_toast_bookmark_load_fail
                }
        }
    }
}
