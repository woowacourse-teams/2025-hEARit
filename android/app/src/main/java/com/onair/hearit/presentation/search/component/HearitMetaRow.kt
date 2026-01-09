package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.toHashtagName
import com.onair.hearit.presentation.toTimeString
import kotlinx.collections.immutable.ImmutableList

@Composable
fun HearitMetaRow(
    keywords: ImmutableList<Keyword>,
    playTime: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp, start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keywords.forEach { keyword ->
            Text(
                text = keyword.toHashtagName(),
                color = Gray2,
                style = HearitTypoGraphy.labelMedium,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = playTime.toTimeString(),
            color = Gray4,
            style = HearitTypoGraphy.labelMedium,
        )
    }
}
