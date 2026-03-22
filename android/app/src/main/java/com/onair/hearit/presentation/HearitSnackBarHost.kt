package com.onair.hearit.presentation

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray4

@Composable
fun HearitSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Gray1,
                contentColor = Gray4,
            )
        },
    )
}
