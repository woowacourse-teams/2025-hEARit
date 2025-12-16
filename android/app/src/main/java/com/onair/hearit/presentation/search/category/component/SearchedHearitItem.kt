package com.onair.hearit.presentation.search.category.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedCategoryHearit
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.toHashtagName
import com.onair.hearit.presentation.toTimeString
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SearchedHearitItem(
    item: SearchedCategoryHearit,
    color: Color,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Gray1,
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onClick(item.id) }
                .padding(vertical = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
        ) {
            Text(
                text = item.title,
                modifier =
                    Modifier
                        .padding(start = 20.dp, end = 8.dp),
                color = Gray4,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = HearitTypoGraphy.bodyLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp, start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.keywords.forEach { keyword ->
                    Text(
                        text = keyword.toHashtagName(),
                        color = Gray2,
                        style = HearitTypoGraphy.labelMedium,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = item.playTime.toTimeString(),
                    color = Gray4,
                    style = HearitTypoGraphy.labelMedium,
                )
            }

            val lastPlayTimeSec = (item.lastPlayTime ?: 0L) / 1000f
            val ratio = lastPlayTimeSec / item.playTime.toFloat()
            CustomLinearProgressBar(
                progress = ratio,
                backgroundColor = DarkGray,
                progressColor = color,
                cornerRadius = 48.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(start = 20.dp, end = 8.dp),
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
private fun SearchedHearitItemPreview() {
    val dummy =
        SearchedCategoryHearit(
            0,
            "드디어 세 번째 레슨, 일희일비 않기. 좀 더 강해져야 돼. 웃어 넘길 수 있게...",
            playTime = 350,
            lastPlayTime = 99999,
            createdAt = "1234",
            keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            category = Category(id = 0L, name = "카테고리이름", colorCode = "#123456"),
        )

    MaterialTheme {
        SearchedHearitItem(dummy, Gray3, {})
    }
}
