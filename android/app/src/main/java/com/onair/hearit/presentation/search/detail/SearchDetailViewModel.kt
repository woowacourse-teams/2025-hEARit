package com.onair.hearit.presentation.search.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.usecase.search.SaveRecentKeywordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
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
    private val hearitRepository: HearitRepository,
    private val recentKeywordRepository: RecentKeywordRepository,
    private val saveRecentKeywordUseCase: SaveRecentKeywordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchDetailUiState())
    val uiState: StateFlow<SearchDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage =
        MutableSharedFlow<Int>(
            extraBufferCapacity = 1,
        )
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    private var fetchJob: Job? = null

    fun loadRecentKeywords() {
        viewModelScope.launch {
            recentKeywordRepository
                .getKeywords()
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
            saveRecentKeywordUseCase(term)
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
            recentKeywordRepository
                .clearKeywords()
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
        val normalized = term.trim()
        if (normalized.isEmpty()) return

        val input = SearchInput.Keyword(term)
        val currentState = _uiState.value
        // 동일 검색어이고 결과가 있거나 로딩 중이면 스킵
        if (currentState.searchInput == input && (currentState.searchedHearits.isNotEmpty() || currentState.isLoading)) return

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

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchInput = null,
                searchedHearits = persistentListOf(),
                currentPage = 0,
                isLastPage = false,
                isLoading = false,
            )
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        val hasTerm = currentState.searchInput is SearchInput.Keyword
        if (!hasTerm || currentState.isLoading || currentState.isLastPage) return
        fetchSearchResults()
    }

    private fun fetchSearchResults() {
        if (fetchJob?.isActive == true) return
        val term = (_uiState.value.searchInput as? SearchInput.Keyword)?.term ?: return

        fetchJob =
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                hearitRepository
                    .getKeywordHearits(term, _uiState.value.currentPage)
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
            }
    }
}
