package com.onair.hearit.data.mapper

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.dto.CategoryHearitResponse
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.PlayingBookmarkResponse
import com.onair.hearit.data.dto.PlayingHistoryResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecentUploadResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.data.dto.SourceResponse
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.domain.model.CategoryHearit
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.PlayingBookmarkHearit
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.SingleHearit
import com.onair.hearit.domain.model.Source
import com.onair.hearit.domain.model.UserInfo

fun RecentHearit.toData(): RecentHearitEntity =
    RecentHearitEntity(
        hearitId = this.id,
        title = this.title,
    )

private fun SearchHearitResponse.Content.toDomain(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        keywords = this.keywords.map { it.toDomain() },
    )

private fun RandomHearitResponse.Content.toDomain(): RandomHearit =
    RandomHearit(
        id = this.id,
        title = this.title,
        categoryColorCode = this.categoryColorCode,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
        keywords =
            this.keywords.map {
                it.toDomain()
            },
        cursorId = this.cursorId,
    )

fun RecentHearitEntity.toDomain(): RecentHearit =
    RecentHearit(
        id = this.hearitId,
        title = this.title,
        lastPosition = this.lastPosition,
    )

fun RecommendHearitResponse.toDomain(): RecommendHearit =
    RecommendHearit(
        id = this.id,
        title = this.title,
        categoryName = this.categoryName,
        categoryColor = this.categoryColor,
    )

fun RandomHearitResponse.toDomain(): CursorResult<RandomHearit> =
    CursorResult(
        items = content.map { it.toDomain() },
        isEmpty = this.isEmpty,
    )

fun HearitResponse.toDomain(): SingleHearit =
    SingleHearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        sources = this.sources.map { it.toDomain() },
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
        category = this.category.toDomain(),
        keywords = this.keywords.map { it.toDomain() },
    )

fun UserInfoResponse.toDomain(): UserInfo =
    UserInfo(
        id = this.id,
        nickname = this.nickname,
        profileImage = this.profileImage,
    )

fun SourceResponse.toDomain(): Source =
    Source(
        name = this.sourceName,
        url = this.sourceUrl,
    )

fun KeywordResponse.toDomain(): Keyword =
    Keyword(
        id = this.id,
        name = this.name,
    )

fun SearchHearitResponse.toDomain(): PageResult<SearchedHearit> =
    PageResult(
        items = content.map { it.toDomain() },
        paging =
            Paging(
                page = page,
                size = size,
                totalPages = totalPages,
                isFirst = isFirst,
                isLast = isLast,
            ),
    )

fun GroupedCategoryHearitResponse.toDomain(): GroupedCategory =
    GroupedCategory(
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        colorCode = this.colorCode,
        hearits = this.categoryHearitResponses.map { it.toDomain() },
    )

fun CategoryHearitResponse.toDomain(): CategoryHearit =
    CategoryHearit(
        hearitId = this.hearitId,
        title = this.title,
        createdAt = this.createdAt,
    )

fun PlayingHistoryResponse.toDomain(): PlayingHistoryHearit =
    PlayingHistoryHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        category = this.category.toDomain(),
    )

fun RecentUploadResponse.toDomain(): RecentUploadHearit =
    RecentUploadHearit(
        id = this.id,
        title = this.title,
        category = this.category.toDomain(),
    )

fun PlayingBookmarkResponse.toDomain(): PlayingBookmarkHearit =
    PlayingBookmarkHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        category = this.category.toDomain(),
    )
