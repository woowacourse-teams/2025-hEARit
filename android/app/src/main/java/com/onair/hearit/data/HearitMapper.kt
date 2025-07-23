package com.onair.hearit.data

import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit

fun SearchHearitResponse.toDomain(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        summary = this.summary,
    )

fun RecommendHearitResponse.toDomain(): RecommendHearit =
    RecommendHearit(
        id = this.id,
        title = this.title,
        desc = this.summary,
    )

fun RandomHearitResponse.toDomain(): RandomHearit =
    RandomHearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )

fun HearitResponse.toDomain(): SingleHearit =
    SingleHearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        playTime = this.playTime,
        createdAt = this.createdAt,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
    )

fun KeywordResponse.toDomain(): Keyword =
    Keyword(
        id = this.id,
        name = this.name,
    )
