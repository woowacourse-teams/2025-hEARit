package com.onair.hearit.presentation.setting.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.onair.hearit.R

@Composable
fun BackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "뒤로가기",
        )
    }
}
