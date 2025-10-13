package com.onair.hearit.data.mapper

import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging

fun BookmarkResponse.Content.toDomain(): Bookmark =
    Bookmark(
        hearitId = hearitId,
        bookmarkId = bookmarkId,
        title = title,
        summary = summary,
        playTime = playTime,
        lastPlayTime = lastPlayTime,
        isFinished = isFinished,
        sources = sources.map { it.toDomain() },
        category = category.toDomain(),
        audioUrl = null,
    )

fun BookmarkResponse.toDomain(): PageResult<Bookmark> =
    PageResult(
        items = content.map { it.toDomain() },
        paging =
            Paging(
                page = page,
                size = size,
                totalPages = totalPages,
                totalElements = totalElements,
                isFirst = isFirst,
                isLast = isLast,
            ),
    )
