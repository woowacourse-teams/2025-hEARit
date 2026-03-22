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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Bookmark

// XML 리소스 기반 색상 정의
private val HearitBlack1 = Color(0xFF272C32)
private val HearitGray4 = Color(0xFFEFF1F2)
private val HearitGray2 = Color(0xFFB2B4B6)
private val HearitPurple3 = Color(0xFF9533F5)

// 폰트 정의
private val PretendardBold = FontFamily(Font(R.font.pretendardbold, FontWeight.Bold))
private val PretendardMedium = FontFamily(Font(R.font.pretendardmedium, FontWeight.Medium))

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
                .padding(
                    start = 8.dp,
                    bottom = 20.dp,
                ) // XML: marginStart="8dp", marginBottom="20dp"
                .background(HearitBlack1)
                .clickable { onItemClick(bookmark.hearitId) }
                .padding(vertical = 0.dp),
        // 내부 패팅은 XML 구조에 따라 조정
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category Initial Box (60dp x 60dp)
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
                fontFamily = PretendardBold,
                fontSize = 20.sp,
                color = HearitGray4,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(top = 12.dp), // XML: marginTop="12dp"
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = bookmark.title,
                    fontFamily = PretendardBold,
                    fontSize = 14.sp,
                    color = HearitGray4,
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
                        modifier = Modifier.padding(end = 20.dp), // XML: marginEnd="20dp"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatPlayTime(bookmark.playTime),
                    fontFamily = PretendardMedium,
                    fontSize = 12.sp,
                    color = HearitGray2,
                    modifier = Modifier.padding(end = 12.dp), // XML: marginEnd="12dp"
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // XML: marginTop="8dp"

            // Progress Bar (height="4dp", marginBottom="12dp")
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
                        .padding(end = 12.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                color = HearitPurple3,
                trackColor = Color(0xFF3B3F43),
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

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
