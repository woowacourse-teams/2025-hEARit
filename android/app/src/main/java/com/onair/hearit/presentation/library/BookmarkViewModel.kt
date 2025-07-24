package com.onair.hearit.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _uiState = MutableLiveData<BookmarkUiState>()
    val uiState: LiveData<BookmarkUiState> = _uiState

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        getUserInfo()
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            memberRepository
                .getUserInfo()
                .onSuccess {
                    _uiState.value = BookmarkUiState.LoggedIn
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
                            _uiState.value = BookmarkUiState.NotLoggedIn
                        }

                        else -> {
                            _toastMessage.value = R.string.all_toast_user_info_load_fail
                        }
                    }
                }
        }

        fun loadBookmarks() {
            // bookmark 조회 후 상태 변경
        }
    }
}
