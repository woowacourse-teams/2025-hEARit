package com.onair.hearit.presentation.search.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.onair.hearit.R
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.presentation.search.SearchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _snackbarMessage = MutableSharedFlow<Int>()
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

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
        fetchCategoryHearits()
    }

    fun fetchCategoryHearits() {
        val currentState = _categoryUiState.value
        val category = currentState.category ?: return
        if (currentState.isLoading || currentState.isLastPage) return

        viewModelScope.launch {
            _categoryUiState.update { it.copy(isLoading = true) }

            hearitRepository
                .getCategoryHearits(category.id, currentState.currentPage)
                .onSuccess { response ->
                    _categoryUiState.update { state ->
                        state.copy(
                            hearits = (state.hearits + response.items).toImmutableList(),
                            isLoading = false,
                            isLastPage = response.paging.isLast,
                            currentPage = response.paging.page + 1,
                        )
                    }
                }.onFailure {
                    _categoryUiState.update { it.copy(isLoading = false) }
                    _snackbarMessage.emit(R.string.category_toast_searched_hearits_load_fail)
                }
        }
    }
}
