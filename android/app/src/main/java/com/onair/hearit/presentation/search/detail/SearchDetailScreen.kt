package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.detail.component.SearchDetailTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val searchInput by viewModel.searchInput.collectAsState()
    val recentKeywords by viewModel.recentKeywords.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.loadRecentKeywords()
    }

    val onSearchExecute: (String) -> Unit = onSearchExecute@{ query ->
        if (query.length < 2) return@onSearchExecute
        viewModel.search(query)
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchDetailTopBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onBackClick = onBackClick,
                onSearch = { onSearchExecute(searchText) },
            )
        },
    ) { padding ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (searchInput) {
                null -> {
                    RecentSearchScreen(
                        keywords = recentKeywords.map { it.term },
                        onKeywordClick = { keyword ->
                            searchText = keyword
                            onSearchExecute(keyword)
                        },
                        onClearAll = { viewModel.clearKeywords() },
                    )
                }

                else -> {
                    SearchResultScreen(
                        hearits = viewModel.searchedHearits.collectAsState().value,
                        onLoadNext = { viewModel.loadNextPage() },
                        onHearitClick = { hearitId ->
                            // 클릭 이벤트 처리
                        },
                    )
                }
            }
        }
    }
}
