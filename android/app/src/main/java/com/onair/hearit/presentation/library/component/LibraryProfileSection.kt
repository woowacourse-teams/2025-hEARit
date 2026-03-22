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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.setting.component.shimmer
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun LibraryProfileSection(
    userInfo: UserInfo,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Profile Image with Shimmer
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

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = userInfo.nickname,
            style = HearitTypoGraphy.headlineSmall,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = onSettingClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_setting),
                contentDescription = "Setting",
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}
