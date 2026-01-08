package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.onair.hearit.presentation.search.detail.component.SearchTopBar

@Composable
fun SearchDetailRoute(
    viewModel: SearchDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val searchInput by viewModel.searchInput.collectAsState()
    val recentKeywords by viewModel.recentKeywords.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }

    Column {
        SearchTopBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onBackClick = onBackClick,
            onSearch = { query -> viewModel.search(query) },
        )

        when (searchInput) {
            null -> {
                SearchRecentScreen(
                    keywords = recentKeywords.map { it.term },
                    onKeywordClick = {
                        searchText = it
                        viewModel.search(it)
                    },
                    onClearAll = { viewModel.clearKeywords() },
                )
            }

            else -> {
                SearchResultScreen(
                    hearits = viewModel.searchedHearits.collectAsState().value,
                    onLoadNext = { viewModel.loadNextPage() },
                    onHearitClick = { /* 클릭 처리 */ },
                )
            }
        }
    }
}
