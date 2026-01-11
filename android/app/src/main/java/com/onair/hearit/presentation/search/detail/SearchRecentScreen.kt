package com.onair.hearit.presentation.search.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.presentation.search.detail.component.SearchRecentItem
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SearchRecentScreen(
    keywords: ImmutableList<String>?,
    onKeywordClick: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack),
    ) {
        RecentSearchHeader(
            showClearAll = !(keywords.isNullOrEmpty()),
            onClearAll = onClearAll,
        )

        when {
            keywords == null -> {
                Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            }

            keywords.isEmpty() -> {
                EmptyRecentSearch()
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 72.dp),
                ) {
                    items(
                        items = keywords,
                        key = { it },
                    ) { keyword ->
                        SearchRecentItem(
                            keyword = keyword,
                            onClick = { onKeywordClick(keyword) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentSearchHeader(
    showClearAll: Boolean,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(start = 20.dp, end = 12.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.search_recent_text),
            style = HearitTypoGraphy.titleSmall,
            color = Gray2,
        )

        if (showClearAll) {
            Text(
                text = stringResource(R.string.search_recent_delete),
                style = HearitTypoGraphy.labelLarge,
                color = Gray2,
                modifier =
                    Modifier
                        .noRippleClickable(onClick = onClearAll)
                        .padding(8.dp),
            )
        }
    }
}

@Composable
private fun EmptyRecentSearch(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = stringResource(R.string.search_no_recent),
            style = HearitTypoGraphy.bodyMedium,
            color = Gray2,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RecentSearchScreenPreview() {
    MaterialTheme {
        SearchRecentScreen(
            keywords =
                persistentListOf(
                    "Android 개발",
                    "Jetpack Compose",
                    "클린 아키텍처",
                    "MVVM 패턴",
                    "코루틴",
                ),
            onKeywordClick = {},
            onClearAll = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EmptyRecentSearchScreenPreview() {
    MaterialTheme {
        SearchRecentScreen(
            keywords = persistentListOf(),
            onKeywordClick = {},
            onClearAll = {},
        )
    }
}
