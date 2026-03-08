package com.onair.hearit.presentation.setting.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.onair.hearit.R
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.component.SettingContent
import com.onair.hearit.presentation.setting.component.SettingTopBar
import com.onair.hearit.presentation.theme.HearitBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
) {
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(id = R.string.privacy_policy_url)
    val termsUrl = stringResource(id = R.string.terms_of_use_url)
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SettingTopBar(
                title = stringResource(R.string.all_setting),
                onBackClick = onBackClick,
            )
        },
        containerColor = HearitBlack,
    ) { padding ->
        SettingContent(
            userInfo = userInfo,
            onProfileClick = onProfileClick,
            onAlarmClick = onAlarmClick,
            onPrivacyPolicyClick = { openUrl(privacyPolicyUrl, context) },
            onTermsClick = { openUrl(termsUrl, context) },
            onOpenSourceClick = { navigateToLicense(context) },
            onLoginClick = onLogin,
            onLogoutClick = onLogout,
            onWithdrawalClick = onWithdraw,
            modifier = Modifier.padding(padding),
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
