package com.onair.hearit.presentation.search.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.usecase.search.ClearRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.GetRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.SaveRecentKeywordUseCase
import com.onair.hearit.domain.usecase.search.SearchHearitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PagingState(
    val currentPage: Int = 0,
    val isLast: Boolean = false,
)

@HiltViewModel
class SearchDetailViewModel @Inject constructor(
    private val getRecentKeywords: GetRecentKeywordsUseCase,
    private val saveRecentKeyword: SaveRecentKeywordUseCase,
    private val clearRecentKeywords: ClearRecentKeywordsUseCase,
    private val searchHearits: SearchHearitsUseCase,
) : ViewModel() {
    private val _recentKeywords = MutableStateFlow<ImmutableList<RecentSearch>>(persistentListOf())
    val recentKeywords: StateFlow<ImmutableList<RecentSearch>> = _recentKeywords.asStateFlow()

    private val _searchedHearits =
        MutableStateFlow<ImmutableList<SearchedHearit>>(persistentListOf())
    val searchedHearits: StateFlow<ImmutableList<SearchedHearit>> = _searchedHearits.asStateFlow()

    private val _searchInput = MutableStateFlow<SearchInput?>(null)
    val searchInput: StateFlow<SearchInput?> = _searchInput.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
    )
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var pagingState = PagingState()

    fun loadRecentKeywords() {
        viewModelScope.launch {
            getRecentKeywords()
                .onSuccess { _recentKeywords.value = it.toImmutableList() }
                .onFailure { throwable ->
                    Timber.w(throwable)
                    _snackbarMessage.emit(R.string.search_toast_recent_keyword_load_fail)
                }
        }
    }

    fun saveKeyword(term: String) {
        viewModelScope.launch {
            saveRecentKeyword(term)
                .onSuccess {
                    loadRecentKeywords()
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _snackbarMessage.emit(R.string.search_toast_recent_hearit_save_fail)
                }
        }
    }

    fun clearKeywords() {
        viewModelScope.launch {
            clearRecentKeywords()
                .onSuccess { count ->
                    _recentKeywords.value = persistentListOf()
                    if (count > 0) {
                        _snackbarMessage.emit(R.string.search_toast_recent_keyword_delete_success)
                    }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _snackbarMessage.emit(R.string.search_toast_recent_keyword_delete_fail)
                }
        }
    }

    fun search(term: String) {
        val input = SearchInput.Keyword(term)
        if (_searchInput.value == input) return

        _searchInput.value = input
        pagingState = PagingState()
        _searchedHearits.value = persistentListOf()

        fetchSearchResults()
    }

    fun loadNextPage() {
        if (_isLoading.value || pagingState.isLast) return
        fetchSearchResults()
    }

    private fun fetchSearchResults() {
        val term = (_searchInput.value as? SearchInput.Keyword)?.term ?: return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                searchHearits(term, pagingState.currentPage)
                    .onSuccess { result ->
                        pagingState =
                            PagingState(
                                currentPage = result.paging.page + 1,
                                isLast = result.paging.isLast,
                            )

                        _searchedHearits.update { current ->
                            (current + result.items).toImmutableList()
                        }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _snackbarMessage.emit(R.string.search_toast_searched_hearits_load_fail)
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
