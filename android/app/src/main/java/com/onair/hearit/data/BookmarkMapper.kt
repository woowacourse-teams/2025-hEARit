package com.onair.hearit.data

import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.domain.model.Bookmark

fun BookmarkResponse.Content.toDomain(): Bookmark =
    Bookmark(
        hearitId = hearitId,
        bookmarkId = bookmarkId,
        title = title,
        summary = summary,
        playTime = playTime,
    )
