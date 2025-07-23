package com.onair.hearit.domain

import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.RandomHearitItem
import com.onair.hearit.domain.model.ScriptLine

fun RandomHearitItem.toHearitShortsItem(
    audioUrl: String,
    script: List<ScriptLine>,
): HearitShorts =
    HearitShorts(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        audioUrl = audioUrl,
        script = script,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )
