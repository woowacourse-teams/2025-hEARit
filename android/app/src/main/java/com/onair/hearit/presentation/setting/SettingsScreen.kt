package com.onair.hearit.presentation.setting

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingViewModel,
) {
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(id = R.string.privacy_policy_url)
    val termsUrl = stringResource(id = R.string.terms_of_use_url)
    val userInfo by viewModel.userInfo.observeAsState(UserInfo.default())

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick = onBackClick)
        },
        containerColor = HearitBlack,
    ) { padding ->
        SettingsContent(
            modifier = Modifier.padding(padding),
            userInfo = userInfo,
            onProfileClick = { /* 내정보 화면으로 이동 */ },
            onPrivacyPolicyClick = { openUrl(privacyPolicyUrl, context) },
            onTermsClick = { openUrl(termsUrl, context) },
            onOpenSourceClick = { navigateToLicense(context) },
            onLogoutClick = { },
            onWithdrawalClick = { },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.all_setting),
                    color = Gray4,
                    style = HearitTypoGraphy.titleLarge,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Gray4,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = HearitBlack,
            ),
    )
}

@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    userInfo: UserInfo,
    onProfileClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onOpenSourceClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        SettingItem(
            text = stringResource(R.string.setting_profile),
            onClick = onProfileClick,
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

        val isLoggedIn = userInfo != UserInfo.default()
        if (isLoggedIn) {
            SettingItem(
                text = stringResource(R.string.setting_logout),
                onClick = onLogoutClick,
            )
        } else {
            SettingItem(
                text = stringResource(R.string.all_login),
                onClick = onLogoutClick,
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
fun SettingItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HearitBlack)
                .clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        )
    }
}

private fun navigateToLicense(context: Context) {
    OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.oss_license_title))
    val intent = Intent(context, OssLicensesMenuActivity::class.java)
    context.startActivity(intent)
}

private fun openUrl(
    url: String,
    context: Context,
) {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}

@Preview(showBackground = true)
@Composable
private fun RegisteredSettingScreenPreview() {
    MaterialTheme {
        SettingsContent(
            userInfo = UserInfo.default(),
            onProfileClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
            onOpenSourceClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotRegisteredSettingScreenPreview() {
    MaterialTheme {
        SettingsContent(
            userInfo = UserInfo(id = 0L, nickname = "hEARit", profileImage = null),
            onProfileClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
            onOpenSourceClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}

@Preview
@Composable
fun SettingItemPreview() {
    SettingItem(
        text = "Sample Setting",
        onClick = {},
    )
}
