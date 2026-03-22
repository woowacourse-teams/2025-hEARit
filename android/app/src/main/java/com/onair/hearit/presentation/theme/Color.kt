package com.onair.hearit.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Red = Color(0xFFFF6464)

// Hearit Project Colors (XML colors.xml 매핑)
val HearitBlack1 = Color(0xFF272C32)
val HearitDarkBlack = Color(0xFF252525)
val HearitDarkGray = Color(0xFF686F75)
val Gray1 = Color(0xFF3B3F43)
val Gray2 = Color(0xFFB2B4B6)
val Gray3 = Color(0xFFD6DADB)
val Gray4 = Color(0xFFEFF1F2)

val HearitPurple1 = Color(0xFFB677F3)
val HearitPurple2 = Color(0xFFAC5FF7)
val HearitPurple3 = Color(0xFF9533F5)
val HearitPurple4 = Color(0xFF795B9B)

// Semantic Colors for Compose
val BackgroundDark = Color(0xFF1A1A1A)
val SurfaceBlack = Color(0xFF121212)
val PrimaryPurple = HearitPurple3

// Backward compatibility (기존에 사용 중인 명칭들)
val HearitBlack = HearitBlack1
val DarkGray = HearitDarkGray

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
