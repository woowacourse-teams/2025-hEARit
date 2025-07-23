package com.onair.hearit.domain

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.model.SingleHearit

fun RandomHearit.toHearitShorts(
    audioUrl: String,
    script: List<ScriptLine>,
): ShortsHearit =
    ShortsHearit(
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
