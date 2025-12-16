package com.onair.hearit.presentation.setting.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.onair.hearit.R
import com.onair.hearit.presentation.setting.component.AlarmContent
import com.onair.hearit.presentation.setting.component.SettingTopBar
import com.onair.hearit.presentation.theme.HearitBlack

@Composable
fun AlarmScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            SettingTopBar(
                title = stringResource(R.string.setting_alarm),
                onBackClick = onBackClick,
            )
        },
        containerColor = HearitBlack,
    ) { padding ->
        AlarmContent(modifier = Modifier.padding(padding))
    }
}
