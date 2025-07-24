package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SearchResultViewModel(
    private val hearitRepository: HearitRepository,
    private val initialSearchTerm: String,
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

    private val isLastPage: Boolean
        get() = paging?.isLast == true

    var currentSearchTerm: String = initialSearchTerm
        private set

    init {
        fetchData(initialSearchTerm, page = 0)
    }

    fun search(searchTerm: String) {
        if (searchTerm == currentSearchTerm) return

        currentSearchTerm = searchTerm
        resetPaging()
        fetchData(searchTerm, page = 0)
    }

    private fun resetPaging() {
        paging = null
        currentPage = 0
    }

    private fun fetchData(
        searchTerm: String,
        page: Int = 0,
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val result = hearitRepository.getSearchHearits(searchTerm, page)

                result
                    .onSuccess { pageResult ->
                        paging = pageResult.paging
                        currentPage = pageResult.paging.page

                        val currentList =
                            if (page == 0) emptyList() else _searchedHearits.value.orEmpty()

                        val updatedList = currentList + pageResult.items
                        _searchedHearits.value = updatedList
                        updateUiState(updatedList)
                    }.onFailure {
                        _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
                    }
            } catch (_: Exception) {
                _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPageIfPossible() {
        if (isLoading || isLastPage) return
        fetchData(currentSearchTerm, currentPage + 1)
    }

    private fun updateUiState(hearits: List<SearchedHearit>) {
        _uiState.value =
            if (hearits.isEmpty()) {
                SearchUiState.NoHearits
            } else {
                SearchUiState.HearitsExist(hearits)
            }
    }
}
