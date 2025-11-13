package com.onair.hearit.presentation.setting.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTopBar(onBackClick: () -> Unit) {
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

@Preview
@Composable
fun SettingTopBarPreview() {
    SettingTopBar(onBackClick = {})
}
