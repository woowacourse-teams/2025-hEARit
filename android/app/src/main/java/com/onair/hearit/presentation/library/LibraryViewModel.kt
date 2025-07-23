package com.onair.hearit.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _bookmarks: MutableLiveData<List<Bookmark>> = MutableLiveData()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    // 테스트용
    private val pageSize = 5

    init {
        fetchData(page = 0)
    }

    private fun fetchData(page: Int) {
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page, pageSize)
                .onSuccess {
                    _bookmarks.value = it
                }.onFailure {
                    _toastMessage.value = R.string.library_toast_bookmark_load_fail
                }
        }
    }
}
