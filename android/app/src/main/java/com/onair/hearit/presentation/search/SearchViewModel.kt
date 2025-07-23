package com.onair.hearit.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
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

    private val _keywords: MutableLiveData<List<Keyword>> = MutableLiveData()
    val keywords: LiveData<List<Keyword>> = _keywords

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData()
    }

    private fun fetchData() {
        getRecommendKeywords()
        getCategories()
    }

    private fun getRecommendKeywords() {
        viewModelScope.launch {
            keywordRepository
                .getRecommendKeywords()
                .onSuccess { keywords ->
                    _keywords.value = keywords
                }.onFailure {
                    _toastMessage.value = R.string.search_toast_keywords_load_fail
                }
        }
    }

    private fun getCategories() {
        viewModelScope.launch {
            categoryRepository
                .getCategories()
                .onSuccess { categories ->
                    _categories.value = categories
                }.onFailure {
                    _toastMessage.value = R.string.all_toast_categories_load_fail
                }
        }
    }
}
