package com.onair.hearit.presentation.library.component

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack1
import com.onair.hearit.presentation.theme.HearitPurple3
import com.onair.hearit.presentation.theme.PretendardFontFamily

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onItemClick: (Long) -> Unit,
    onOptionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 20.dp)
                .background(HearitBlack1)
                .clickable { onItemClick(bookmark.hearitId) }
                .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(bookmark.category.colorCode))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = bookmark.category.name.take(1),
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray4,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = bookmark.title,
                        fontFamily = PretendardFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Gray4,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    if (bookmark.isFinished == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_hearit_finished),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatPlayTime(bookmark.playTime),
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Gray2,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val lastPlayTimeSec = (bookmark.lastPlayTime ?: 0L) / 1000f
            val progress =
                if (bookmark.playTime > 0) {
                    lastPlayTimeSec / bookmark.playTime.toFloat()
                } else {
                    0f
                }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                color = HearitPurple3,
                trackColor = Gray1,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_option_vertical),
            contentDescription = null,
            tint = Color.White,
            modifier =
                Modifier
                    .size(28.dp)
                    .clickable { onOptionClick(bookmark.bookmarkId) },
        )
    }
}

private fun formatPlayTime(playTime: Int): String {
    val minutes = playTime / 60
    val seconds = playTime % 60
    return "%02d:%02d".format(minutes, seconds)
}
