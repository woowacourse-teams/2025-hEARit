package com.onair.hearit.presentation.library.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R

private val PretendardBold = FontFamily(Font(R.font.pretendardbold, FontWeight.Bold))
private val PretendardMedium = FontFamily(Font(R.font.pretendardmedium, FontWeight.Medium))
private val HearitGray4 = Color(0xFFEFF1F2)
private val HearitGray2 = Color(0xFFB2B4B6)

@Composable
fun LibraryHeaderSection(
    totalCount: Int,
    isPlaying: Boolean,
    onPlayAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp), // XML: marginStart="20dp", marginEnd="20dp"
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.library_bookmarked_hearit_title),
                fontFamily = PretendardBold,
                fontSize = 20.sp, // @style/pretendard_main_title
                color = HearitGray4
            )

            Spacer(modifier = Modifier.height(4.dp)) // XML: marginTop="4dp"

            Text(
                text = stringResource(id = R.string.library_bookmarked_hearit_total_count, totalCount),
                fontFamily = PretendardMedium,
                fontSize = 14.sp, // @style/pretendard_body
                color = HearitGray2
            )
        }

        Image(
            painter = painterResource(id = if (isPlaying) R.drawable.img_pause else R.drawable.img_play),
            contentDescription = "Play All",
            modifier = Modifier
                .size(56.dp) // XML: 56dp
                .padding(4.dp) // XML: padding="4dp"
                .clickable { onPlayAllClick() }
        )
    }
}
