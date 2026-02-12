package com.onair.hearit.presentation.search.category

import androidx.compose.runtime.Immutable
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.PagingState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CategoryUiState(
    val category: Category,
    val hearits: ImmutableList<SearchedHearit> = persistentListOf(),
    val pagingState: PagingState = PagingState(),
)
