package com.onair.hearit.presentation.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun LibraryLoginRequiredView(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
        // hearit_black1
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 56.dp),
        ) {
            Text(
                text = stringResource(id = R.string.library_guide_when_no_login),
                style = HearitTypoGraphy.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFBB86FC)) // Primary Purple (bg_purple3_radius_8dp 참고)
                        .clickable { onLoginClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.all_login),
                    style = HearitTypoGraphy.headlineSmall,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun LibraryEmptyBookmarkView(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
        // hearit_black1
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.library_guide_when_no_bookmark),
            style = HearitTypoGraphy.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 64.dp),
        )
    }
}
