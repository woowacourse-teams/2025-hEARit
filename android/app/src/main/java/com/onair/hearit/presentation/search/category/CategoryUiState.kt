package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Immutable
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.SearchedCategoryHearit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CategoryUiState(
    val category: Category? = null,
    val hearits: ImmutableList<SearchedCategoryHearit> = persistentListOf(),
    val isLoading: Boolean = false,
    val isLastPage: Boolean = false,
)
