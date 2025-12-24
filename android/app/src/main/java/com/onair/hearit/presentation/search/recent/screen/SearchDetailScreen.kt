package com.onair.hearit.presentation.search.recent.screen

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
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.recent.component.SearchDetailTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDetailScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchInput by viewModel.searchInput.collectAsState()
    val recentKeywords by viewModel.recentKeywords.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.getRecentKeywords()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchDetailTopBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onBackClick = onBackClick,
                onSearch = { query ->
                    if (query.length >= 2) {
                        viewModel.setSearchInput(SearchInput.Keyword(query))
                        viewModel.saveRecentKeyword(query)
                    }
                },
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
                    // 최근 검색어 화면
                    RecentSearchScreen(
                        keywords = recentKeywords?.map { it.term },
                        onKeywordClick = { keyword ->
                            searchText = keyword
                            viewModel.setSearchInput(SearchInput.Keyword(keyword))
                            viewModel.saveRecentKeyword(keyword)
                        },
                        onClearAll = { viewModel.deleteKeywords() },
                    )
                }

                else -> {
                    // 검색 결과 화면
//                    SearchResultScreen(
//                        searchInput = searchInput!!,
//                        viewModel = viewModel,
//                    )
                }
            }
        }
    }
}
