package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.toHashtagName
import com.onair.hearit.presentation.toTimeString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HearitItem(
    title: String,
    keywords: ImmutableList<Keyword>,
    playTime: Int,
    lastPlayTime: Long?,
    progressColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Gray1,
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onClick() }
                .padding(vertical = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
        ) {
            HearitTitle(title = title)
            Spacer(modifier = Modifier.height(4.dp))
            HearitMetaRow(
                keywords = keywords,
                playTime = playTime,
            )
            HearitProgressBar(
                lastPlayTimeMillis = lastPlayTime,
                totalPlayTimeSec = playTime,
                progressColor = progressColor,
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .padding(end = 8.dp),
            tint = Gray4,
        )
    }
}

@Composable
private fun HearitTitle(
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

@Composable
private fun HearitMetaRow(
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

@Composable
@Preview(showBackground = true)
private fun HearitItemPreview() {
    MaterialTheme {
        HearitItem(
            title = "드디어 세 번째 레슨, 일희일비 않기. 좀 더 강해져야 돼. 웃어 넘길 수 있게...",
            keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            playTime = 350,
            lastPlayTime = 99999,
            progressColor = HearitPurple1,
            onClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun HearitItemWithColorPreview() {
    MaterialTheme {
        HearitItem(
            title = "드디어 세 번째 레슨, 일희일비 않기. 좀 더 강해져야 돼. 웃어 넘길 수 있게...",
            keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            playTime = 350,
            lastPlayTime = 99999,
            progressColor = Gray3,
            onClick = {},
        )
    }
}
