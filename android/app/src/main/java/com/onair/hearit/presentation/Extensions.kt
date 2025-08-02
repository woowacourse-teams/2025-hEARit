package com.onair.hearit.presentation

import android.content.Context

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.pxToDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()
