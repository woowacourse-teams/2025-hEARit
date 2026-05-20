package com.onair.hearit.widget

import android.content.Context
import android.content.Intent
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity

fun widgetMainIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

fun widgetDetailIntent(
    context: Context,
    hearitId: Long,
    source: String,
): Intent =
    PlayerDetailActivity
        .newIntent(
            context = context,
            hearitId = hearitId,
            source = source,
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
