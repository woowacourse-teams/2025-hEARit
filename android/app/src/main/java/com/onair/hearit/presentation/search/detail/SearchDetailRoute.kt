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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchText by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadRecentKeywords()
    }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collect { stringResId ->
            val message = context.getString(stringResId)
            snackbarHostState.showSnackbar(message)
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
                onSearch = { query ->
                    viewModel.search(query)
                    viewModel.saveKeyword(query)
                    isFocused = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                isFocused = isFocused,
                focusRequester = focusRequester,
                onClick = { isFocused = true },
            )

            when {
                searchInput == null -> {
                    SearchRecentScreen(
                        keywords = recentKeywords?.map { it.term },
                        onKeywordClick = { keyword ->
                            searchText = keyword
                            viewModel.search(keyword)
                            viewModel.saveKeyword(keyword)
                            isFocused = false
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        onClearAll = { viewModel.clearKeywords() },
                    )
                }

                else -> {
                    SearchResultScreen(
                        hearits = searchedHearits,
                        onLoadNext = { viewModel.loadNextPage() },
                        onHearitClick = { /* 클릭 처리 */ },
                        isLoading = isLoading,
                    )
                }
            }
        }
    }
}
