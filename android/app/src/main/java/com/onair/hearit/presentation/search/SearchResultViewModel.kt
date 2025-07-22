package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.SearchedHearitItem
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SearchResultViewModel(
    private val hearitRepository: HearitRepository,
    private val initialSearchTerm: String,
) : ViewModel() {
    private val _searchedHearits: MutableLiveData<List<SearchedHearitItem>> = MutableLiveData()
    val searchedHearits: LiveData<List<SearchedHearitItem>> = _searchedHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    var currentSearchTerm: String = initialSearchTerm
        private set

    init {
        fetchData(initialSearchTerm)
    }

    fun search(searchTerm: String) {
        if (searchTerm == currentSearchTerm) return

        currentSearchTerm = searchTerm
        fetchData(searchTerm)
    }

    private fun fetchData(searchTerm: String) {
        viewModelScope.launch {
            hearitRepository
                .getSearchHearits(searchTerm)
                .onSuccess { _searchedHearits.value = it }
                .onFailure {
                    _toastMessage.value = R.string.search_toast_searched_hearits_load_fail
                }
        }
    }
}
