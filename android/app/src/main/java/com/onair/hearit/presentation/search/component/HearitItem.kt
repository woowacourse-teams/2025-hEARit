package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitPurple1
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
