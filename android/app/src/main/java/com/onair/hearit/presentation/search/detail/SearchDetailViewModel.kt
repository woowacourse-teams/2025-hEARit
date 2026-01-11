package com.onair.hearit.presentation.search.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.usecase.search.ClearRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.GetRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.SaveRecentKeywordUseCase
import com.onair.hearit.domain.usecase.search.SearchHearitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class SearchDetailViewModel @Inject constructor(
    private val getRecentKeywords: GetRecentKeywordsUseCase,
    private val saveRecentKeyword: SaveRecentKeywordUseCase,
    private val clearRecentKeywords: ClearRecentKeywordsUseCase,
    private val searchHearits: SearchHearitsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchDetailUiState())
    val uiState: StateFlow<SearchDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage =
        MutableSharedFlow<Int>(
            extraBufferCapacity = 1,
        )
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    fun loadRecentKeywords() {
        viewModelScope.launch {
            getRecentKeywords()
                .onSuccess { keywords ->
                    _uiState.update { it.copy(recentKeywords = keywords.toImmutableList()) }
                }.onFailure { throwable ->
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
                    _uiState.update { it.copy(recentKeywords = persistentListOf()) }
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
        if (_uiState.value.searchInput == input) return

        _uiState.update {
            it.copy(
                searchInput = input,
                searchedHearits = persistentListOf(),
                currentPage = 0,
                isLastPage = false,
            )
        }

        fetchSearchResults()
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLastPage) return
        fetchSearchResults()
    }

    private fun fetchSearchResults() {
        val currentState = _uiState.value
        val term = (currentState.searchInput as? SearchInput.Keyword)?.term ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                searchHearits(term, currentState.currentPage)
                    .onSuccess { result ->
                        _uiState.update { state ->
                            state.copy(
                                searchedHearits = (state.searchedHearits + result.items).toImmutableList(),
                                currentPage = result.paging.page + 1,
                                isLastPage = result.paging.isLast,
                                isLoading = false,
                            )
                        }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _uiState.update { it.copy(isLoading = false) }
                        _snackbarMessage.emit(R.string.search_toast_searched_hearits_load_fail)
                    }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(isLoading = false) }
                _snackbarMessage.emit(R.string.search_toast_searched_hearits_load_fail)
            }
        }
    }
}
