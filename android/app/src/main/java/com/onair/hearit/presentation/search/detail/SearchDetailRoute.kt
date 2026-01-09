package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.onair.hearit.presentation.search.detail.component.SearchDetailTopBar

@Composable
fun SearchDetailRoute(
    viewModel: SearchDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val recentKeywords by viewModel.recentKeywords.collectAsState()
    val searchedHearits by viewModel.searchedHearits.collectAsState()
    val searchInput by viewModel.searchInput.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    var searchText by rememberSaveable { mutableStateOf("") }

    fun performSearch(query: String) {
        viewModel.search(query)
        viewModel.saveKeyword(query)
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecentKeywords()
        focusRequester.requestFocus()
    }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collect { stringResId ->
            snackbarHostState.showSnackbar(context.getString(stringResId))
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 48.dp),
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            SearchDetailTopBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onBackClick = onBackClick,
                onSearch = ::performSearch,
                focusRequester = focusRequester,
            )

            when {
                searchInput == null -> {
                    SearchRecentScreen(
                        keywords = recentKeywords?.map { it.term },
                        onKeywordClick = { keyword ->
                            searchText = keyword
                            performSearch(keyword)
                        },
                        onClearAll = viewModel::clearKeywords,
                    )
                }

                else -> {
                    SearchResultScreen(
                        hearits = searchedHearits,
                        onLoadNext = viewModel::loadNextPage,
                        onHearitClick = { /* 클릭 처리 */ },
                        isLoading = isLoading,
                    )
                }
            }
        }
    }
}
