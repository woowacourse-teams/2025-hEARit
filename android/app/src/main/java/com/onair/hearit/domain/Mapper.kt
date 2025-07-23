package com.onair.hearit.domain

import RandomHearitItem
import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.ScriptLine

fun RandomHearitItem.toHearitShorts(
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

fun SingleHearit.toHearit(
    audioUrl: String,
    script: List<ScriptLine>,
): Hearit =
    Hearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        audioUrl = audioUrl,
        script = script,
        playTime = this.playTime,
        createdAt = this.createdAt,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
    )
