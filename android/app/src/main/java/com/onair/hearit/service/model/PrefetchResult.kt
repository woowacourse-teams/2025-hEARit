package com.onair.hearit.service.model

import androidx.media3.common.MediaItem

data class PrefetchResult(
    val items: List<MediaItem>,
    val nextPage: Int?,
)
