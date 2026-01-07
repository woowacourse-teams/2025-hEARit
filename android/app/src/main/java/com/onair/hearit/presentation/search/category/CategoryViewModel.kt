package com.onair.hearit.presentation.search.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.search.SearchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hearitRepository: HearitRepository,
) : ViewModel() {
    private val categoryArgs: SearchRoute.Category = savedStateHandle.toRoute()

    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

    private val _toastMessage = SingleLiveData<Int?>()
    val toastMessage: LiveData<Int?> = _toastMessage

    init {
        _categoryUiState.value =
            CategoryUiState(
                category =
                    Category(
                        categoryArgs.id,
                        categoryArgs.name,
                        categoryArgs.colorCode,
                    ),
            )
        fetchCategoryHearits(isInitial = true)
    }

    fun fetchCategoryHearits(isInitial: Boolean) {
        val currentState = _categoryUiState.value
        val category = currentState.category ?: return
        if (currentState.isLoading) return
        if (!isInitial && currentState.isLastPage) return

        val targetPage = if (isInitial) 0 else currentState.currentPage

        viewModelScope.launch {
            _categoryUiState.update { it.copy(isLoading = true) }

            hearitRepository
                .getCategoryHearits(category.id, targetPage)
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
                            currentPage = if (isInitial) 1 else state.currentPage + 1,
                        )
                    }
                }.onFailure {
                    _categoryUiState.update { it.copy(isLoading = false) }
                    _toastMessage.value = R.string.category_toast_searched_hearits_load_fail
                }
        }
    }
}
