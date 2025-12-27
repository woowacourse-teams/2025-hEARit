package com.onair.hearit.presentation.home.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.onair.hearit.presentation.theme.Gray2

fun String.toComposeColor(fallback: Color = Gray2): Color =
    runCatching {
        val colorString = if (startsWith("#")) this else "#$this"
        Color(colorString.toColorInt())
    }.getOrElse { fallback }
