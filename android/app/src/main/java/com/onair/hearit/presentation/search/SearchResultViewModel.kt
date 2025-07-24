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
    private val _searchedHearits: MutableLiveData<List<SearchedHearit>> = MutableLiveData()
    val searchedHearits: LiveData<List<SearchedHearit>> = _searchedHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private var paging: Paging? = null
    private var isLoading = false

    var currentSearchTerm: String = initialSearchTerm
        private set

    init {
        fetchData(initialSearchTerm)
    }

    fun search(searchTerm: String) {
        if (searchTerm == currentSearchTerm) return

        currentSearchTerm = searchTerm
        paging = null
        _searchedHearits.value = emptyList()
        fetchData(searchTerm)
    }

    private fun fetchData(
        searchTerm: String,
        page: Int = 0,
    ) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            hearitRepository
                .getSearchHearits(searchTerm, page = page)
                .onSuccess { pageResult ->
                    paging = pageResult.paging
                    val currentList =
                        if (page == 0) emptyList() else _searchedHearits.value.orEmpty()
                    _searchedHearits.value = currentList + pageResult.items
                }.onFailure {
                    _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
                }

            isLoading = false
        }
    }

    fun loadNextPageIfPossible() {
        val currentPaging = paging ?: return
        if (currentPaging.isLast) return

        val nextPage = currentPaging.page + 1
        fetchData(currentSearchTerm, nextPage)
    }
}
