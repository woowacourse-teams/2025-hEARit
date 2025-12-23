package com.onair.hearit.presentation.search

import com.onair.hearit.domain.model.SearchedHearit

sealed class SearchUiState {
    data object NoHearits : SearchUiState()

    data class HearitsExist(
        val hearits: List<SearchedHearit>,
    ) : SearchUiState()
}
