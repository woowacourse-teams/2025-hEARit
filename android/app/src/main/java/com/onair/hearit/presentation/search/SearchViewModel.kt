package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.KeywordRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SearchViewModel(
    private val categoryRepository: CategoryRepository,
    private val keywordRepository: KeywordRepository,
) : ViewModel() {
    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>> = _categories

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private lateinit var paging: Paging
    private var currentPage = 0
    private var isLastPage = false

    init {
        fetchData()
    }

    private fun fetchData() {
        getCategories()
    }

    private fun getCategories() {
        viewModelScope.launch {
            categoryRepository
                .getCategories(page = 0)
                .onSuccess { pageCategories ->
                    paging = pageCategories.paging
                    _categories.value = pageCategories.items
                    isLastPage = paging.isLast
                }.onFailure {
                    _toastMessage.value = R.string.all_toast_categories_load_fail
                }
        }
    }
}
