package com.onair.hearit.presentation.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.setting.component.shimmer

private val PretendardBold = FontFamily(Font(R.font.pretendardbold, FontWeight.Bold))
private val HearitGray4 = Color(0xFFEFF1F2)

@Composable
fun LibraryProfileSection(
    userInfo: UserInfo,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .padding(start = 20.dp, top = 20.dp, end = 12.dp),
        // XML margin 기준
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Profile Image (48dp x 48dp)
        SubcomposeAsyncImage(
            model = userInfo.profileImage,
            contentDescription = null,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .shimmer()
                            .background(Color(0xFF1A1A1A)),
                )
            },
            error = {
                Icon(
                    painter = painterResource(id = R.drawable.img_default_profile),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            },
        )

        Spacer(modifier = Modifier.width(16.dp)) // XML marginStart="16dp"

        Text(
            text = userInfo.nickname,
            fontFamily = PretendardBold,
            fontSize = 24.sp, // @style/pretendard_nickname
            color = HearitGray4,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onSettingClick,
            modifier = Modifier.size(48.dp), // XML size="48dp"
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_setting),
                contentDescription = "Setting",
                tint = Color.Unspecified, // 아이콘 본연의 색상 유지
            )
        }
    }
}
