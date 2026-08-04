package com.onair.hearit.presentation.library

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _uiState = MutableStateFlow<BookmarkUiState>(NotLoggedIn)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    private val _userInfo = MutableStateFlow(DEFAULT_USER_INFO)
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: SingleLiveData<Int> = _toastMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var nextPage: Int? = 0

    init {
        fetchUserInfo()
    }

    fun refreshBookmarks() {
        nextPage = 0
        val previousBookmarks = _bookmarks.value
        val currentUserInfo = userInfo.value

        if (currentUserInfo == DEFAULT_USER_INFO) return

        fetchDataWithRestore(page = 0, previousBookmarks = previousBookmarks)
    }

    fun loadNextPage() {
        val page: Int = nextPage ?: return
        val previousBookmarks: List<Bookmark> = _bookmarks.value
        fetchDataWithRestore(page = page, previousBookmarks = previousBookmarks)
    }

    private fun fetchDataWithRestore(
        page: Int,
        previousBookmarks: List<Bookmark>,
    ) {
        if (isLoading.value || nextPage == null) return

        _isLoading.value = true
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = page, size = null, filter = "all")
                .onSuccess { pageResult ->
                    val newList: List<Bookmark> =
                        if (page == 0) {
                            pageResult.items
                        } else {
                            val currentList: List<Bookmark> = _bookmarks.value
                            currentList + pageResult.items
                        }
                    _bookmarks.value = newList
                    _totalCount.value = pageResult.paging.totalElements
                    _uiState.value = if (_bookmarks.value.isEmpty()) NoBookmarks else LoggedIn

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
                        _bookmarks.value.filterNot { it.bookmarkId == bookmarkId }
                    _bookmarks.value = updatedList

                    val newCount = _totalCount.value - 1
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
