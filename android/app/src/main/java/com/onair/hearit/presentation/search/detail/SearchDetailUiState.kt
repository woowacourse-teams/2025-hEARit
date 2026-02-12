package com.onair.hearit.presentation.search.detail

import androidx.compose.runtime.Immutable
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SearchDetailUiState(
    val recentKeywords: ImmutableList<RecentSearch> = persistentListOf(),
    val searchedHearits: ImmutableList<SearchedHearit> = persistentListOf(),
    val searchInput: SearchInput? = null,
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val isLastPage: Boolean = false,
)
