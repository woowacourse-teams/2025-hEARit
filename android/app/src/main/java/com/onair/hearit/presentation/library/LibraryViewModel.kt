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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _bookmarks = MutableLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    private val _uiState = MutableLiveData<BookmarkUiState>()
    val uiState: LiveData<BookmarkUiState> = _uiState

    private val _userInfo = MutableStateFlow(DEFAULT_USER_INFO)
    val userInfo = _userInfo.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var nextPage: Int? = 0

    init {
        fetchUserInfo()
    }

    fun refreshBookmarks() {
        nextPage = 0
        val previousBookmarks = _bookmarks.value.orEmpty()
        val currentUserInfo = userInfo.value

        if (currentUserInfo == DEFAULT_USER_INFO) return

        fetchDataWithRestore(page = 0, previousBookmarks = previousBookmarks)
    }

    fun loadNextPage() {
        val page: Int = nextPage ?: return
        val previousBookmarks: List<Bookmark> = _bookmarks.value.orEmpty()
        fetchDataWithRestore(page = page, previousBookmarks = previousBookmarks)
    }

    private fun fetchDataWithRestore(
        page: Int,
        previousBookmarks: List<Bookmark>,
    ) {
        if (isLoading.value == true || nextPage == null) return

        _isLoading.value = true
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = page, size = null, filter = "all")
                .onSuccess { pageResult ->
                    val newList: List<Bookmark> =
                        if (page == 0) {
                            pageResult.items
                        } else {
                            val currentList: List<Bookmark> = _bookmarks.value.orEmpty()
                            currentList + pageResult.items
                        }
                    _bookmarks.value = newList
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
                    _bookmarks.value = previousBookmarks
                    _toastMessage.value = R.string.library_toast_bookmark_load_fail
                    _uiState.value = if (previousBookmarks.isEmpty()) NoBookmarks else LoggedIn
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

    private fun fetchUserInfo() {
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
                            _userInfo.value = DEFAULT_USER_INFO
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }
    }

    companion object {
        private val DEFAULT_USER_INFO = UserInfo.default()
    }
}
