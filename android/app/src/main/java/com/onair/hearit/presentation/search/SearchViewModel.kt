package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.presentation.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val hearitRepository: HearitRepository,
    private val recentKeywordRepository: RecentKeywordRepository,
) : ViewModel() {
    private val _searchUiState = MutableLiveData<SearchUiState>()
    val searchUiState: LiveData<SearchUiState> = _searchUiState

    private val _recentKeywords = MutableStateFlow<List<RecentSearch>?>(null)
    val recentKeywords: StateFlow<List<RecentSearch>?> = _recentKeywords.asStateFlow()

    private val _searchedHearits = MutableLiveData<List<SearchedHearit>>()
    val searchedHearits: LiveData<List<SearchedHearit>> = _searchedHearits

    private val _toastMessage = SingleLiveData<Int?>()
    val toastMessage: LiveData<Int?> = _toastMessage

    private var currentInput: SearchInput? = null

    private val _searchInput = MutableStateFlow<SearchInput?>(null)
    val searchInput: StateFlow<SearchInput?> = _searchInput.asStateFlow()

    private var paging: Paging? = null
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    fun setSearchInput(input: SearchInput.Keyword) {
        if (currentInput == input) return
        _searchInput.value = input
        resetPaging()
        _searchedHearits.value = emptyList()

        fetchKeywordHearits(input.term, true)
    }

    fun refreshSearchResults() {
        resetPaging()
        _searchedHearits.value = emptyList()
        fetchKeywordHearits(
            (_searchInput.value as? SearchInput.Keyword)?.term ?: return,
            isInitial = true,
        )
    }

    fun getRecentKeywords() {
        viewModelScope.launch {
            recentKeywordRepository
                .getKeywords()
                .onSuccess { keywords ->
                    _recentKeywords.value = keywords
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.search_toast_recent_keyword_load_fail
                }
        }
    }

    fun deleteKeywords() {
        viewModelScope.launch {
            recentKeywordRepository
                .clearKeywords()
                .onSuccess { count ->
                    if (count > 0) {
                        _recentKeywords.value = emptyList()
                        _toastMessage.value = R.string.search_toast_recent_keyword_delete_success
                    }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.search_toast_recent_keyword_delete_fail
                }
        }
    }

    fun loadNextPageIfPossible() {
        if (isLoading || paging?.isLast == true) return
        val term = (_searchInput.value as? SearchInput.Keyword)?.term ?: return
        fetchKeywordHearits(term, isInitial = false)
    }

    fun fetchKeywordHearits(
        term: String,
        isInitial: Boolean,
    ) {
        if (isLoading) return
        if (!isInitial && isLastPage) return

        isLoading = true

        viewModelScope.launch {
            try {
                val page = if (isInitial) 0 else currentPage + 1
                val result = hearitRepository.getKeywordHearits(term, page)

                result
                    .onSuccess { pageResult ->
                        paging = pageResult.paging
                        currentPage = pageResult.paging.page

                        val updatedList =
                            if (isInitial) {
                                pageResult.items
                            } else {
                                _searchedHearits.value.orEmpty() + pageResult.items
                            }

                        _searchedHearits.value = updatedList
                        updateUiState(updatedList)
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
                    }
            } finally {
                isLoading = false
            }
        }
    }

    fun saveRecentKeyword(recentKeyword: String) {
        viewModelScope.launch {
            val recentSearch =
                RecentSearch(
                    term = recentKeyword,
                    searchedAt = System.currentTimeMillis(),
                )
            recentKeywordRepository
                .saveKeyword(recentSearch)
                .onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.search_toast_recent_hearit_save_fail
                }
        }
    }

    private fun updateUiState(hearits: List<SearchedHearit>) {
        _searchUiState.value =
            if (hearits.isEmpty()) {
                SearchUiState.NoHearits
            } else {
                SearchUiState.HearitsExist(hearits)
            }
    }

    private fun resetPaging() {
        paging = null
        currentPage = 0
        isLastPage = false
    }
}
