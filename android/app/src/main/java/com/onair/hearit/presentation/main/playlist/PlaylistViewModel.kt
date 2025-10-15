package com.onair.hearit.presentation.main.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _bookmarks: MutableLiveData<List<Bookmark>> = MutableLiveData()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var nextPage: Int? = 0

    init {
        refreshPlaylist()
    }

    fun refreshPlaylist() {
        if (_isLoading.value == true) return
        nextPage = 0
        _bookmarks.value = emptyList()
        fetchBookmarks(page = 0)
    }

    fun loadNextPage() {
        val page = nextPage ?: return
        fetchBookmarks(page)
    }

    private fun fetchBookmarks(page: Int) {
        if (_isLoading.value == true || nextPage == null) return

        _isLoading.value = true
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = null, size = null, filter = "all")
                .onSuccess { pageResult ->
                    val currentList = _bookmarks.value.orEmpty()
                    _bookmarks.value = currentList + pageResult.items

                    nextPage =
                        if (!pageResult.paging.isLast) {
                            pageResult.paging.page + 1
                        } else {
                            null
                        }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.library_toast_bookmark_load_fail
                }
            _isLoading.value = false
        }
    }
}
