package com.onair.hearit.presentation.search.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.usecase.search.ClearRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.GetRecentKeywordsUseCase
import com.onair.hearit.domain.usecase.search.SaveRecentKeywordUseCase
import com.onair.hearit.domain.usecase.search.SearchHearitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _recentKeywords = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recentKeywords: StateFlow<List<RecentSearch>> = _recentKeywords.asStateFlow()

    private val _searchedHearits = MutableStateFlow<List<SearchedHearit>>(emptyList())
    val searchedHearits: StateFlow<List<SearchedHearit>> = _searchedHearits.asStateFlow()

    private val _searchInput = MutableStateFlow<SearchInput?>(null)
    val searchInput: StateFlow<SearchInput?> = _searchInput.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<Int>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private var paging: Paging? = null
    private var currentPage = 0
    private var isLoading = false

    fun loadRecentKeywords() {
        viewModelScope.launch {
            getRecentKeywords()
                .onSuccess { _recentKeywords.value = it }
                .onFailure { throwable ->
                    Timber.Forest.w(throwable)
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
                    Timber.Forest.w(throwable)
                    _snackbarMessage.emit(R.string.search_toast_recent_hearit_save_fail)
                }
        }
    }

    fun clearKeywords() {
        viewModelScope.launch {
            clearRecentKeywords()
                .onSuccess { count ->
                    if (count > 0) {
                        _recentKeywords.value = emptyList()
                        _snackbarMessage.emit(R.string.search_toast_recent_keyword_delete_success)
                    }
                }.onFailure { throwable ->
                    Timber.Forest.w(throwable)
                    _snackbarMessage.emit(R.string.search_toast_recent_keyword_delete_fail)
                }
        }
    }

    fun search(term: String) {
        val input = SearchInput.Keyword(term)
        if (_searchInput.value == input) return

        _searchInput.value = input
        resetPaging()
        _searchedHearits.value = emptyList()

        fetch(isInitial = true)
    }

    fun loadNextPage() {
        if (isLoading || paging?.isLast == true) return
        fetch(isInitial = false)
    }

    private fun fetch(isInitial: Boolean) {
        val term = (_searchInput.value as? SearchInput.Keyword)?.term ?: return

        viewModelScope.launch {
            isLoading = true
            val page = if (isInitial) 0 else currentPage + 1

            try {
                searchHearits(term, page)
                    .onSuccess { result ->
                        paging = result.paging
                        currentPage = result.paging.page

                        _searchedHearits.value =
                            if (isInitial) {
                                result.items
                            } else {
                                _searchedHearits.value + result.items
                            }
                    }.onFailure { throwable ->
                        Timber.Forest.w(throwable)
                        _snackbarMessage.emit(R.string.search_toast_searched_hearits_load_fail)
                    }
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetPaging() {
        paging = null
        currentPage = 0
        isLoading = false
    }
}
