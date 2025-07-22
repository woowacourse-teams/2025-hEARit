package com.onair.hearit.data

import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.RecommendHearit
import com.onair.hearit.domain.SearchedHearit

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

fun RandomHearitResponse.toDomain(): RandomHearitItem =
    RandomHearitItem(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )
