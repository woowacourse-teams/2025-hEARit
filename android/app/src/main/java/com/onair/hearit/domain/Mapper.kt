package com.onair.hearit.domain

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.model.SingleHearit

fun RandomHearit.toHearitShorts(
    audioUrl: String,
    script: List<ScriptLine>,
): ShortsHearit =
    ShortsHearit(
        id = this.id,
        title = this.title,
        audioUrl = audioUrl,
        script = script,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
        keywords = this.keywords,
        categoryColorCode = this.categoryColorCode,
        cursorId = this.cursorId,
    )

fun SingleHearit.toHearit(
    audioUrl: String,
    script: List<ScriptLine>,
): Hearit =
    Hearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        sources = this.sources,
        audioUrl = audioUrl,
        script = script,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
        category = this.category,
        keywords = this.keywords,
    )

fun SingleHearit.toPlaybackInfo(
    audioUrl: String,
    title: String,
    startPosition: Long? = null,
    source: String,
): PlaybackInfo =
    PlaybackInfo(
        hearitId = this.id,
        audioUrl = audioUrl,
        title = title,
        lastPosition = startPosition,
        source = source,
    )

fun SearchInput.term(): String =
    when (this) {
        is SearchInput.Keyword -> this.term
        is SearchInput.Category -> this.name
    }
