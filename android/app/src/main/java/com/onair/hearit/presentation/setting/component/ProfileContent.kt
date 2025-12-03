package com.onair.hearit.presentation.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun ProfileContent(
    userInfo: UserInfo?,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        ProfileImage(
            imageUrl = userInfo?.profileImage,
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(40.dp))

        NicknameCard(
            nickname = userInfo?.nickname ?: "",
        )

        Spacer(modifier = Modifier.height(40.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Gray4,
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomInfo(appVersion = appVersion)

        Spacer(modifier = Modifier.height(160.dp))
    }
}

@Composable
private fun NicknameCard(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .border(
                    width = 2.dp,
                    color = HearitPurple1,
                    shape = RoundedCornerShape(24.dp),
                )
                .background(
                    color = HearitBlack,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 60.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nickname,
            style = HearitTypoGraphy.titleMedium,
            color = Gray4,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun NicknameCardPreview() {
    NicknameCard(nickname = "hEARit")
}

@Composable
private fun BottomInfo(
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "앱 버전 $appVersion",
            style = HearitTypoGraphy.bodyMedium,
            color = Gray4,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.all_hearit_email),
            style = HearitTypoGraphy.bodyMedium,
            color = Gray4,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    MaterialTheme {
        ProfileContent(
            userInfo =
                UserInfo(
                    id = 0L,
                    nickname = "hEARit",
                    profileImage = null,
                ),
            appVersion = "v1.3.4-DEBUG",
        )
    }
}
