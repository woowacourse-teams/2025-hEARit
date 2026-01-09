package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun HearitTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(start = 20.dp, end = 8.dp),
        color = Gray4,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = HearitTypoGraphy.bodyLarge,
    )
}
