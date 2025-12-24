package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
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
    private val categoryRepository: CategoryRepository,
    private val hearitRepository: HearitRepository,
    private val recentKeywordRepository: RecentKeywordRepository,
) : ViewModel() {
    private val _searchUiState = MutableLiveData<SearchUiState>()
    val searchUiState: LiveData<SearchUiState> = _searchUiState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _recentKeywords = MutableStateFlow<List<RecentSearch>?>(null)
    val recentKeywords: StateFlow<List<RecentSearch>?> = _recentKeywords.asStateFlow()

    private val _currentCategory = MutableStateFlow<Category?>(null)
    val currentCategory: StateFlow<Category?> = _currentCategory.asStateFlow()

    private val _searchedHearits = MutableLiveData<List<SearchedHearit>>()
    val searchedHearits: LiveData<List<SearchedHearit>> = _searchedHearits

    private val _categoryHearits = MutableStateFlow<List<SearchedCategoryHearit>>(emptyList())
    val categoryHearits: StateFlow<List<SearchedCategoryHearit>> = _categoryHearits

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
        _categoryHearits.value = emptyList()

        fetchKeywordHearits(input.term, true)
    }

    fun setCurrentCategory(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        if (_currentCategory.value?.id == id) return

        resetPaging()
        _categoryHearits.value = emptyList()

        _currentCategory.value =
            Category(
                id = id,
                name = name,
                colorCode = colorCode,
            )

        fetchCategoryHearits(isInitial = true)
    }

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
        val input = currentInput ?: return

        when (input) {
            is SearchInput.Category -> fetchCategoryHearits(isInitial)
            is SearchInput.Keyword -> fetchKeywordHearits(input.term, isInitial)
        }
    }

    fun fetchCategoryHearits(isInitial: Boolean) {
        val category = currentCategory.value ?: return

        if (isLoading) return
        if (!isInitial && isLastPage) return

        isLoading = true

        viewModelScope.launch {
            hearitRepository
                .getCategoryHearits(category.id, if (isInitial) 0 else currentPage)
                .onSuccess { response ->
                    _categoryHearits.value =
                        if (isInitial) {
                            currentPage = 0
                            response.items
                        } else {
                            _categoryHearits.value + response.items
                        }

                    paging = response.paging
                    isLastPage = response.paging.isLast
                    currentPage++
                    isLoading = false
                }.onFailure {
                    isLoading = false
                    _toastMessage.value = R.string.category_toast_searched_hearits_load_fail
                }
        }
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

    fun clearCategoryHearits() {
        _categoryHearits.value = emptyList()
        _currentCategory.value = null
    }

    fun clearToastMessage() {
        _toastMessage.value = null
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
