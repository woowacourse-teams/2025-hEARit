package com.onair.hearit.presentation.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.model.isLoggedIn
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.Red
import com.onair.hearit.presentation.util.noRippleClickable

@Composable
fun SettingContent(
    userInfo: UserInfo?,
    onProfileClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onOpenSourceClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack),
    ) {
        SettingItem(
            text = stringResource(R.string.setting_profile),
            onClick = onProfileClick,
        )

        SettingItem(
            text = stringResource(R.string.setting_notification),
            onClick = onAlarmClick,
        )

        SettingItem(
            text = stringResource(R.string.privacy_policy),
            onClick = onPrivacyPolicyClick,
        )

        SettingItem(
            text = stringResource(R.string.terms_of_use),
            onClick = onTermsClick,
        )

        SettingItem(
            text = stringResource(R.string.open_license),
            onClick = onOpenSourceClick,
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Gray4.copy(alpha = 0.5f),
        )

        val isLoggedIn = userInfo?.isLoggedIn() == true
        if (isLoggedIn) {
            SettingItem(
                text = stringResource(R.string.setting_logout),
                onClick = onLogoutClick,
            )
        } else {
            SettingItem(
                text = stringResource(R.string.all_login),
                onClick = onLoginClick,
            )
        }

        if (isLoggedIn) {
            SettingItem(
                text = stringResource(R.string.setting_withdrawal),
                onClick = onWithdrawalClick,
                textColor = Red,
            )
        }
    }
}

@Composable
private fun SettingItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisteredSettingScreenPreview() {
    MaterialTheme {
        SettingContent(
            userInfo = UserInfo.default(),
            onProfileClick = {},
            onAlarmClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
            onOpenSourceClick = {},
            onLoginClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotRegisteredSettingScreenPreview() {
    MaterialTheme {
        SettingContent(
            userInfo = UserInfo(id = 0L, nickname = "hEARit", profileImage = null),
            onProfileClick = {},
            onAlarmClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
            onOpenSourceClick = {},
            onLoginClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingItemPreview() {
    SettingItem(
        text = "Sample Setting",
        onClick = {},
    )
}
