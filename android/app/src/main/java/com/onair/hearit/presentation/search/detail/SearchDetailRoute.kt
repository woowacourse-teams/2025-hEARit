package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.onair.hearit.presentation.search.detail.component.SearchDetailTopBar

@Composable
fun SearchDetailRoute(
    viewModel: SearchDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val recentKeywords by viewModel.recentKeywords.collectAsState()
    val searchedHearits by viewModel.searchedHearits.collectAsState()

    var searchText by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    Column {
        SearchDetailTopBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onBackClick = onBackClick,
            onSearch = { query ->
                viewModel.search(query)
                isFocused = false
            },
            isFocused = isFocused,
            focusRequester = focusRequester,
            onClick = { isFocused = true },
        )

        when {
            searchText.isEmpty() -> {
                SearchRecentScreen(
                    keywords = recentKeywords.map { it.term },
                    onKeywordClick = {
                        searchText = it
                        viewModel.search(it)
                        isFocused = false
                    },
                    onClearAll = { viewModel.clearKeywords() },
                )
            }

            else -> {
                SearchResultScreen(
                    hearits = searchedHearits,
                    onLoadNext = { viewModel.loadNextPage() },
                    onHearitClick = { /* 클릭 처리 */ },
                )
            }
        }
    }
}
