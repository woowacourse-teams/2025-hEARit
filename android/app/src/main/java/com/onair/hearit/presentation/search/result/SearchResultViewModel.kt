package com.onair.hearit.presentation.search.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.term
import com.onair.hearit.domain.usecase.GetSearchResultUseCase
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.search.SearchUiState
import kotlinx.coroutines.launch

class SearchResultViewModel(
    private val recentKeywordRepository: RecentKeywordRepository,
    private val getSearchResultUseCase: GetSearchResultUseCase,
    initialInput: SearchInput,
) : ViewModel() {
    private val _uiState = MutableLiveData<SearchUiState>()
    val uiState: LiveData<SearchUiState> = _uiState

    private val _searchedHearits = MutableLiveData<List<SearchedHearit>>()
    val searchedHearits: LiveData<List<SearchedHearit>> = _searchedHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private var paging: Paging? = null
    private var currentPage: Int = 0
    private var isLoading = false

    private var currentInput: SearchInput = initialInput

    private val currentSearchTerm: String
        get() = currentInput.term()

    init {
        fetchData(isInitial = true)
        saveRecentKeyword()
    }

    fun loadNextPageIfPossible() {
        if (isLoading || paging?.isLast == true) return
        fetchData(isInitial = false)
    }

    fun search(term: String) {
        currentInput = SearchInput.Keyword(term)
        resetPaging()
        fetchData(isInitial = true)
    }

    private fun resetPaging() {
        currentPage = 0
        paging = null
    }

    private fun fetchData(isInitial: Boolean) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val page = if (isInitial) 0 else currentPage + 1
                val result = getSearchResultUseCase(currentInput, page)

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
                    }.onFailure {
                        _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
                    }
            } finally {
                isLoading = false
            }
        }
    }

    private fun updateUiState(hearits: List<SearchedHearit>) {
        _uiState.value =
            if (hearits.isEmpty()) {
                SearchUiState.NoHearits
            } else {
                SearchUiState.HearitsExist(hearits)
            }
    }

    private fun saveRecentKeyword() {
        viewModelScope.launch {
            recentKeywordRepository
                .saveKeyword(currentSearchTerm)
                .onFailure {
                    _toastMessage.value = R.string.search_toast_recent_hearit_save_fail
                }
        }
    }
}
