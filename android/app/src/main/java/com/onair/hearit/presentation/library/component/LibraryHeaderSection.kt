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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.PretendardFontFamily

@Composable
fun LibraryHeaderSection(
    totalCount: Int,
    isPlaying: Boolean,
    onPlayAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(id = R.string.library_bookmarked_hearit_title),
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray4,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    stringResource(
                        id = R.string.library_bookmarked_hearit_total_count,
                        totalCount,
                    ),
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Gray2,
            )
        }

        Image(
            painter = painterResource(id = if (isPlaying) R.drawable.img_pause else R.drawable.img_play),
            contentDescription = "Play All",
            modifier =
                Modifier
                    .size(56.dp)
                    .padding(4.dp)
                    .clickable { onPlayAllClick() },
        )
    }
}
