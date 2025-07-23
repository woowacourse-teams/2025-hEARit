package com.onair.hearit.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.Category
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class HomeViewModel(
    private val categoryRepository: CategoryRepository,
    private val hearitRepository: HearitRepository,
) : ViewModel() {
    private val _recommendHearits: MutableLiveData<List<RecommendHearit>> = MutableLiveData()
    val recommendHearits: LiveData<List<RecommendHearit>> = _recommendHearits

    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>> = _categories

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            hearitRepository
                .getRecommendHearits()
                .onSuccess { recommendHearits ->
                    _recommendHearits.value = recommendHearits
                }.onFailure {
                    _toastMessage.value = R.string.home_toast_recommend_load_fail
                }
        }

        viewModelScope.launch {
            categoryRepository
                .getCategories()
                .onSuccess { categories ->
                    _categories.value = categories
                }.onFailure {
                    _toastMessage.value = R.string.home_toast_categories_load_fail
                }
        }
    }
}
