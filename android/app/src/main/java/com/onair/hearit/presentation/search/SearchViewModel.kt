package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.usecase.GetSearchResultUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(
    private val categoryRepository: CategoryRepository,
    private val recentKeywordRepository: RecentKeywordRepository,
    private val getSearchResultUseCase: GetSearchResultUseCase,
    initialInput: SearchInput?,
) : ViewModel() {
    private val _searchUiState = MutableLiveData<SearchUiState>()
    val searchUiState: LiveData<SearchUiState> = _searchUiState

    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>> = _categories

    private val _recentKeywords: MutableLiveData<List<RecentSearch>> = MutableLiveData()
    val recentKeywords: LiveData<List<RecentSearch>> = _recentKeywords

    private val _searchedHearits = MutableLiveData<List<SearchedHearit>>()
    val searchedHearits: LiveData<List<SearchedHearit>> = _searchedHearits

    private val currentInput = initialInput

    val currentCategory: Category? =
        (currentInput as? SearchInput.Category)?.let {
            Category(
                id = it.id,
                name = it.name,
                colorCode = it.colorCode,
            )
        }

    private val _categoryHearits = MutableStateFlow<List<SearchedHearit>>(emptyList())
    val categoryHearits: StateFlow<List<SearchedHearit>> = _categoryHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private var paging: Paging? = null
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    fun refreshSearchResults() {
        resetPaging()
        _searchedHearits.value = emptyList()
        fetchResultData(isInitial = true)
    }

    fun getCategories() {
        viewModelScope.launch {
            categoryRepository
                .getCategories(page = 0)
                .onSuccess { pageCategories ->
                    paging = pageCategories.paging
                    _categories.value = pageCategories.items
                    isLastPage = pageCategories.paging.isLast
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.all_toast_categories_load_fail
                }
        }
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
        fetchResultData(isInitial = false)
    }

    fun fetchResultData(isInitial: Boolean) {
        if (isLoading) return
        val input = currentInput ?: return
        isLoading = true

        viewModelScope.launch {
            try {
                val page = if (isInitial) 0 else currentPage + 1
                val result = getSearchResultUseCase(input, page)

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

                        _categoryHearits.value = updatedList
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
            recentKeywordRepository
                .saveKeyword(recentKeyword)
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
