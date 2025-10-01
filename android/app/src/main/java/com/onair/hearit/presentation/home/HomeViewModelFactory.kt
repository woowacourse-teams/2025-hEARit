package com.onair.hearit.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val hearitRepository = RepositoryProvider.hearitRepository
        val memberRepository = RepositoryProvider.memberRepository
        val playingHistoryRepository = RepositoryProvider.playingHistoryRepository
        return HomeViewModel(
            bookmarkRepository,
            hearitRepository,
            memberRepository,
            playingHistoryRepository,
        ) as T
    }
}
