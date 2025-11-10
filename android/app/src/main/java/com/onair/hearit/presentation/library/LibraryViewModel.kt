package com.onair.hearit.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.exception.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.UserRepository
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.library.BookmarkUiState.LoggedIn
import com.onair.hearit.presentation.library.BookmarkUiState.NoBookmarks
import com.onair.hearit.presentation.library.BookmarkUiState.NotLoggedIn
import kotlinx.coroutines.launch
import timber.log.Timber

class LibraryViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _bookmarks = MutableLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    private val _uiState = MutableLiveData<BookmarkUiState>()
    val uiState: LiveData<BookmarkUiState> = _uiState

    private val _userInfo = MutableLiveData(UserInfo.default())
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var nextPage: Int? = 0

    init {
        getUserInfo()
    }

    fun refreshBookmarks() {
        nextPage = 0
        _bookmarks.value = emptyList()
        if (userInfo.value != UserInfo.default()) {
            fetchData(page = 0)
        }
    }

    fun loadNextPage() {
        nextPage?.let { fetchData(it) }
    }

    private fun fetchData(page: Int) {
        if (isLoading.value == true || nextPage == null) return

        _isLoading.value = true
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = page, size = null, filter = "all")
                .onSuccess { pageResult ->
                    val currentList = _bookmarks.value.orEmpty()
                    _bookmarks.value = currentList + pageResult.items
                    _totalCount.value = pageResult.paging.totalElements
                    _uiState.value = if (_bookmarks.value.isNullOrEmpty()) NoBookmarks else LoggedIn

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

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            bookmarkRepository
                .deleteBookmark(bookmarkId)
                .onSuccess {
                    val updatedList =
                        _bookmarks.value?.filterNot { it.bookmarkId == bookmarkId }.orEmpty()
                    _bookmarks.value = updatedList

                    val newCount = (_totalCount.value ?: 0) - 1
                    _totalCount.value = newCount.coerceAtLeast(0)

                    if (updatedList.isEmpty()) {
                        _uiState.value = NoBookmarks
                    }
                }.onFailure {
                    _toastMessage.value = R.string.all_toast_delete_bookmark_fail
                }
        }
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            userRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _userInfo.value = userInfo
                    refreshBookmarks()
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _uiState.value = NotLoggedIn
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                    _userInfo.value = UserInfo.default()
                }
        }
    }
}
