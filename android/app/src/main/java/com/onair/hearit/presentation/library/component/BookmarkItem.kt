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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.theme.HearitTypoGraphy

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
                .clickable { onItemClick(bookmark.hearitId) }
                .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category Initial Box
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
                style = HearitTypoGraphy.headlineSmall,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = bookmark.title,
                    style = HearitTypoGraphy.titleMedium,
                    color = Color(0xFFE0E0E0), // hearit_gray4
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

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatPlayTime(bookmark.playTime),
                    style = HearitTypoGraphy.bodySmall,
                    color = Color(0xFF9E9E9E), // hearit_gray2
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            val progress =
                if (bookmark.playTime > 0L) {
                    (bookmark.lastPlayTime ?: 0L).toFloat() / bookmark.playTime.toFloat()
                } else {
                    0f
                }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFBB86FC), // Primary Purple
                trackColor = Color(0xFF373737), // Background Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Option Button
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
    val minutes = (playTime / 60)
    val seconds = (playTime % 60)
    return "%02d:%02d".format(minutes, seconds)
}
