package com.onair.hearit.presentation.main.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class PlaylistViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        return PlaylistViewModel(
            bookmarkRepository,
        ) as T
    }
}
