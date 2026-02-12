package com.onair.hearit.presentation.search.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.repository.CategoryRepository
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchMainViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _searchMainUiState = MutableStateFlow(SearchMainUiState())
    val searchMainUiState: StateFlow<SearchMainUiState> = _searchMainUiState.asStateFlow()

    private val _snackbarMessage =
        MutableSharedFlow<Int>(
            extraBufferCapacity = 1,
        )
    val snackbarMessage: SharedFlow<Int> = _snackbarMessage.asSharedFlow()

    fun fetchCategories() {
        if (_searchMainUiState.value.isLoading) return

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
                    _snackbarMessage.emit(R.string.all_toast_categories_load_fail)
                }
        }
    }
}
