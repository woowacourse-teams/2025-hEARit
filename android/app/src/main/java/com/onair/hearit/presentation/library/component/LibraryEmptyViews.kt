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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.PretendardFontFamily
import com.onair.hearit.presentation.theme.PrimaryPurple
import com.onair.hearit.presentation.theme.SurfaceBlack

@Composable
fun LibraryLoginRequiredView(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(SurfaceBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 56.dp),
        ) {
            Text(
                text = stringResource(id = R.string.library_guide_when_no_login),
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray4,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPurple)
                        .clickable { onLoginClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.all_login),
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
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
                .background(SurfaceBlack),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.library_guide_when_no_bookmark),
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Gray4,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 64.dp),
        )
    }
}
