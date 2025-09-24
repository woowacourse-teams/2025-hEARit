package com.onair.hearit.service.model

import com.onair.hearit.domain.model.PlaybackInfo

data class LibraryLoadResult(
    val items: List<PlaybackInfo>,
    val seedIndex: Int,
)
