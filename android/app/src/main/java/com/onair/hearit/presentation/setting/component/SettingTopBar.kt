package com.onair.hearit.presentation.setting.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.HearitTopAppBarDefaults
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = HearitTypoGraphy.titleLarge,
            )
        },
        navigationIcon = { BackIconButton(onClick = onBackClick) },
        colors = HearitTopAppBarDefaults.topAppBarColors(),
    )
}

@Preview
@Composable
fun SettingTopBarPreview() {
    SettingTopBar(title = stringResource(R.string.all_setting), onBackClick = {})
}
