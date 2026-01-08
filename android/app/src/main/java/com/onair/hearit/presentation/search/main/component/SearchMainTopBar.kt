package com.onair.hearit.presentation.search.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HearitBlack),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.menu_search),
                    color = Gray4,
                    style = HearitTypoGraphy.headlineMedium,
                )
            },
            colors =
                TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = HearitBlack,
                    scrolledContainerColor = HearitBlack,
                ),
            scrollBehavior = scrollBehavior,
        )

        SearchBarInput(
            onSearchBarClick = onSearchBarClick,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = stringResource(R.string.search_category_text),
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 12.dp),
            color = Gray4,
            style = HearitTypoGraphy.titleLarge,
        )
    }
}

@Composable
fun SearchBarInput(
    onSearchBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(HearitBlack)
                    .noRippleClickable(onClick = onSearchBarClick)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.search_topbar_hint),
                modifier = Modifier.weight(1f),
                color = Gray2,
                style = HearitTypoGraphy.bodyMedium,
            )

            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Gray4,
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun SearchMainTopBarPreview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    MaterialTheme {
        SearchMainTopBar(scrollBehavior, {})
    }
}

@Composable
@Preview(showBackground = true)
fun SearchBarPreview() {
    MaterialTheme {
        SearchBarInput({ })
    }
}
