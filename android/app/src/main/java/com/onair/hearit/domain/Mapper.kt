package com.onair.hearit.domain

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.model.SearchInput

fun Hearit.toPlaybackInfo(
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
