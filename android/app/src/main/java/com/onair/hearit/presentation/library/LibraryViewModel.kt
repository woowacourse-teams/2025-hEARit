package com.onair.hearit.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _bookmarks: MutableLiveData<List<Bookmark>> = MutableLiveData()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _uiState = MutableLiveData<BookmarkUiState>()
    val uiState: LiveData<BookmarkUiState> = _uiState

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData(page = 0)
        getUserInfo()
    }

    fun fetchData(page: Int) {
        viewModelScope.launch {
            bookmarkRepository
                .getBookmarks(page = page, size = null)
                .onSuccess {
                    _bookmarks.value = it
                }.onFailure {
                    _toastMessage.value = R.string.library_toast_bookmark_load_fail
                }
        }
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            memberRepository
                .getUserInfo()
                .onSuccess { userInfo ->
                    _uiState.value = BookmarkUiState.LoggedIn
                    _userInfo.value = userInfo
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            _uiState.value = BookmarkUiState.NotLoggedIn
                        }

                        else -> {
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                    val defaultUserInfo = UserInfo(-1, "hEARit", null)
                    _userInfo.value = defaultUserInfo
                }
        }
    }
}
