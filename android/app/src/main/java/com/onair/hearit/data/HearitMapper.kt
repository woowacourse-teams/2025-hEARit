package com.onair.hearit.data

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit
import com.onair.hearit.domain.model.UserInfo

fun RecentHearit.toData(): RecentHearitEntity =
    RecentHearitEntity(
        hearitId = this.id,
        title = this.title,
    )

fun SearchHearitResponse.toDomain(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        summary = this.summary,
    )

fun RecentHearitEntity.toDomain(): RecentHearit =
    RecentHearit(
        id = this.hearitId,
        title = this.title,
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

fun UserInfoResponse.toDomain(): UserInfo =
    UserInfo(
        id = this.id,
        nickname = this.nickname,
        profileImage = this.profileImage,
    )
