package com.onair.hearit.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

val PromptLogo: TextStyle =
    TextStyle(
        fontFamily = Prompt,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PromptSplashLogo: TextStyle =
    TextStyle(
        fontFamily = Prompt,
        fontWeight = FontWeight.W700,
        fontSize = 60.sp,
        color = Color.White,
        platformStyle = NoFontPadding,
    )

val PretendardNickname: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PretendardMainTitle: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 20.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PretendardSubTitle: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PretendardCategory: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PretendardBottomTitle: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 14.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )

val PretendardBody: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        platformStyle = NoFontPadding,
    )

val PretendardSettingBody: TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        color = Gray4,
        platformStyle = NoFontPadding,
    )
