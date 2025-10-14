package com.onair.hearit.presentation

import com.onair.hearit.analytics.HearitSource

interface HearitClickListener {
    fun onClick(
        hearitId: Long,
        source: HearitSource,
    )
}
