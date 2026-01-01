package com.onair.hearit.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.onair.hearit.presentation.theme.Gray2

fun String.toComposeColor(fallback: Color = Gray2): Color =
    runCatching {
        val colorString = if (startsWith("#")) this else "#$this"
        Color(colorString.toColorInt())
    }.getOrElse { fallback }

/**
 * 색상 문자열을 안전하게 Color로 변환
 * 실패 시 fallback 색상 반환
 */
@Composable
fun rememberSafeColor(
    colorString: String,
    fallback: Color = Color.Black,
): Color =
    remember(colorString, fallback) {
        runCatching {
            Color(colorString.toColorInt())
        }.getOrElse { fallback }
    }

// 비-Composable 버전도 유용할 수 있음
fun String.toSafeColor(fallback: Color = Color.Black): Color = runCatching { Color(this.toColorInt()) }.getOrElse { fallback }
