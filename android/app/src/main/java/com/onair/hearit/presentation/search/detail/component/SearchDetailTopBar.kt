package com.onair.hearit.presentation.search.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun SearchDetailTopBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    minSearchQueryLength: Int = 2,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    fun performSearch() {
        if (searchText.length >= minSearchQueryLength) {
            onSearch(searchText)
            keyboardController?.hide()
            focusRequester.freeFocus()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HearitBlack)
                .statusBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Gray4,
                )
            }

            Text(
                text = "검색",
                style = HearitTypoGraphy.titleLarge,
                color = Gray4,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(HearitBlack)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier =
                        Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .clickable { focusRequester.requestFocus() },
                    textStyle =
                        HearitTypoGraphy.bodyLarge.copy(
                            color = Gray4,
                        ),
                    cursorBrush = SolidColor(Gray4),
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = { performSearch() },
                        ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_topbar_hint),
                                    color = Gray2,
                                    style = HearitTypoGraphy.bodyMedium,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchTextChange("")
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "지우기",
                            modifier = Modifier.size(20.dp),
                            tint = Gray4,
                        )
                    }
                }

                IconButton(
                    onClick = ::performSearch,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "검색",
                        modifier = Modifier.size(24.dp),
                        tint = Gray4,
                    )
                }
            }

            HorizontalDivider(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = Gray4,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchDetailTopBarEmptyPreview() {
    MaterialTheme {
        var searchText by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        SearchDetailTopBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onBackClick = {},
            onSearch = {},
            focusRequester = focusRequester,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchDetailTopBarWithTextPreview() {
    MaterialTheme {
        var searchText by remember { mutableStateOf("안드로이드") }
        val focusRequester = remember { FocusRequester() }

        SearchDetailTopBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onBackClick = {},
            onSearch = {},
            focusRequester = focusRequester,
        )
    }
}
