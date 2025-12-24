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
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.search.category.CategoryUiState
import com.onair.hearit.presentation.search.main.SearchMainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _searchMainUiState = MutableStateFlow(SearchMainUiState())
    val searchMainUiState: StateFlow<SearchMainUiState> = _searchMainUiState.asStateFlow()

    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

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

    fun setCurrentCategory(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        if (_categoryUiState.value.category?.id == id) return

        resetPaging()

        val category = Category(id, name, colorCode)
        _categoryUiState.update {
            CategoryUiState(
                category = category,
                hearits = persistentListOf(),
                isLoading = true,
            )
        }

        fetchCategoryHearits(isInitial = true)
    }

    fun refreshSearchResults() {
        resetPaging()
        _searchedHearits.value = emptyList()
        fetchResultData(isInitial = true)
    }

    fun loadCategories() {
        viewModelScope.launch {
            _searchMainUiState.update { it.copy(isLoading = true) }

            categoryRepository
                .getCategories(page = 0)
                .onSuccess { pageCategories ->
                    _searchMainUiState.update {
                        it.copy(
                            categories = pageCategories.items.toImmutableList(),
                            isLoading = false,
                        )
                    }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _searchMainUiState.update { it.copy(isLoading = false) }
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
        val category = _categoryUiState.value.category
        if (category == null) {
            return
        }
        if (isLoading) {
            return
        }
        if (!isInitial && isLastPage) {
            return
        }

        isLoading = true

        viewModelScope.launch {
            _categoryUiState.update { it.copy(isLoading = true) }

            hearitRepository
                .getCategoryHearits(category.id, if (isInitial) 0 else currentPage)
                .onSuccess { response ->
                    _categoryUiState.update { state ->
                        state.copy(
                            hearits =
                                if (isInitial) {
                                    response.items.toImmutableList()
                                } else {
                                    (state.hearits + response.items).toImmutableList()
                                },
                            isLoading = false,
                            isLastPage = response.paging.isLast,
                        )
                    }
                    currentPage = if (isInitial) 1 else currentPage + 1
                    isLastPage = response.paging.isLast
                }.onFailure {
                    _categoryUiState.update { it.copy(isLoading = false) }
                    _toastMessage.value = R.string.category_toast_searched_hearits_load_fail
                }

            isLoading = false
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
        _categoryUiState.update { CategoryUiState() }
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
