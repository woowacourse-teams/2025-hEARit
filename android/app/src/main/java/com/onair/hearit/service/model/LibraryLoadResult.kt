package com.onair.hearit.service.model

import androidx.media3.common.MediaItem

data class LibraryLoadResult(
    val items: List<MediaItem>,
    val seedIndex: Int,
)
