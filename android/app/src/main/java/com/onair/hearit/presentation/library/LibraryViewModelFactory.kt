package com.onair.hearit.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class LibraryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val memberRepository = RepositoryProvider.memberRepository
        val getBookmarkUseCase = UseCaseProvider.getBookmarkUseCase
        return LibraryViewModel(
            bookmarkRepository,
            memberRepository,
            getBookmarkUseCase,
        ) as T
    }
}
