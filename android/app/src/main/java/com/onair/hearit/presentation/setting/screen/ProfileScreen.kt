package com.onair.hearit.presentation.setting.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.R
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.component.ProfileContent
import com.onair.hearit.presentation.setting.component.SettingTopBar
import com.onair.hearit.presentation.theme.HearitBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SettingViewModel,
    onBackClick: () -> Unit,
) {
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()
    val appVersion = viewModel.appVersion

    Scaffold(
        topBar = {
            SettingTopBar(
                title = stringResource(R.string.setting_profile),
                onBackClick = onBackClick,
            )
        },
        containerColor = HearitBlack,
    ) { padding ->
        ProfileContent(
            userInfo = userInfo,
            appVersion = appVersion,
            modifier = Modifier.padding(padding),
        )
    }
}
