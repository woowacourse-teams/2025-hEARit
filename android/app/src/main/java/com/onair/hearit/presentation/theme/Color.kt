package com.onair.hearit.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Red = Color(0xFFFF6464)

val HearitBlack = Color(0xFF272C32)
val DarkGray = Color(0xFF686F75)
val Gray1 = Color(0xFF3B3F43)
val Gray2 = Color(0xFFB2B4B6)
val Gray3 = Color(0xFFD6DADB)
val Gray4 = Color(0xFFEFF1F2)
val HearitPurple1 = Color(0xFFB677F3)
val HearitPurple2 = Color(0xFFAC5FF7)

object HearitTopAppBarDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topAppBarColors() =
        TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = HearitBlack,
            navigationIconContentColor = Gray4,
            titleContentColor = Gray4,
        )
}
