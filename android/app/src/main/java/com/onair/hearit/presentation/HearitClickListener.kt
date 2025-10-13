package com.onair.hearit.presentation

import com.onair.hearit.domain.model.HearitSource

interface HearitClickListener {
    fun onClick(
        hearitId: Long,
        source: HearitSource,
    )
}
