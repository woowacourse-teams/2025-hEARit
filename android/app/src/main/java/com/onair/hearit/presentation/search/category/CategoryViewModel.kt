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

    private val _categoryUiState =
        MutableStateFlow(
            CategoryUiState(
                category =
                    Category(
                        id = categoryArgs.id,
                        name = categoryArgs.name,
                        colorCode = categoryArgs.colorCode,
                    ),
            ),
        )
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    fun fetchCategoryHearits() {
        val state = _categoryUiState.value
        val paging = state.pagingState

        if (paging.isLoading) return
        if (!paging.canLoadMore()) return

        viewModelScope.launch {
            _categoryUiState.update { it.copy(pagingState = it.pagingState.startLoading()) }

            hearitRepository
                .getCategoryHearits(state.category.id, state.pagingState.currentPage)
                .onSuccess { response ->
                    _categoryUiState.update { state ->
                        state.copy(
                            hearits = (state.hearits + response.items).toImmutableList(),
                            pagingState =
                                state.pagingState.finishLoading(
                                    nextPage = response.paging.page + 1,
                                    isLast = response.paging.isLast,
                                ),
                        )
                    }
                }.onFailure {
                    _categoryUiState.update { it.copy(pagingState = it.pagingState.failLoading()) }
                    _snackbarMessage.tryEmit(R.string.category_toast_searched_hearits_load_fail)
                }
        }
    }
}
