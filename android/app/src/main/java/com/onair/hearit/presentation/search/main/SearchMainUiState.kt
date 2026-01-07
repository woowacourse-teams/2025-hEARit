package com.onair.hearit.presentation.search.main

import androidx.compose.runtime.Immutable
import com.onair.hearit.domain.model.Category
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SearchMainUiState(
    val categories: ImmutableList<Category> = persistentListOf(),
    val isLoading: Boolean = false,
)
