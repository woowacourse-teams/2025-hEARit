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

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun NicknameCard(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier =
            modifier
                .border(
                    width = 2.dp,
                    color = HearitPurple1,
                    shape = shape,
                ).background(
                    color = HearitBlack,
                    shape = shape,
                ).padding(horizontal = 60.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nickname,
            color = Gray4,
            textAlign = TextAlign.Center,
            style = HearitTypoGraphy.titleMedium,
        )
    }
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
            text = stringResource(R.string.setting_app_version, appVersion),
            color = Gray4,
            style = HearitTypoGraphy.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.all_hearit_email),
            color = Gray4,
            style = HearitTypoGraphy.bodyMedium,
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
            appVersion = "1.3.4-DEBUG",
        )
    }
}

@Preview
@Composable
private fun NicknameCardPreview() {
    NicknameCard(nickname = "hEARit")
}

@Preview(showBackground = true)
@Composable
private fun BottomInfoPreview() {
    BottomInfo(appVersion = "1.3.4")
}
