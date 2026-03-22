package com.onair.hearit.data.mapper

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.LikeResponse
import com.onair.hearit.data.dto.PlayingHistoryResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.RecommendationCategoryHearitResponse
import com.onair.hearit.data.dto.SearchHearitsResponse
import com.onair.hearit.data.dto.SourceResponse
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.domain.model.CategoryHearit
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.Like
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.model.Source
import com.onair.hearit.domain.model.UserInfo
import kotlinx.collections.immutable.toImmutableList

fun RecentHearit.toData(): RecentHearitEntity =
    RecentHearitEntity(
        hearitId = this.id,
        title = this.title,
    )

private fun SearchHearitsResponse.Content.toSearchedHearit(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        keywords = this.keywords.map { it.toDomain() }.toImmutableList(),
    )

private fun HearitsResponse.Content.toSearchedHearit(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        keywords = this.keywords.map { it.toDomain() }.toImmutableList(),
        category = this.category.toDomain(),
    )

private fun HearitsResponse.Content.toRecentUploadHearit(): RecentUploadHearit =
    RecentUploadHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        lastPlayTime = this.lastPlayTime,
        createdAt = this.createdAt,
        keywords = this.keywords.map { it.toDomain() },
        category = this.category.toDomain(),
    )

private fun ExploreHearitResponse.Content.toDomain(): ExploreHearit =
    ExploreHearit(
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
        audioUrl = null,
        script = null,
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

fun ExploreHearitResponse.toDomain(): CursorResult<ExploreHearit> =
    CursorResult(
        items = content.map { it.toDomain() },
        isEmpty = this.isEmpty,
    )

fun HearitResponse.toDomain(): Hearit =
    Hearit(
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
        audioUrl = null,
        script = null,
        like = this.like.toDomain(),
        viewCount = this.viewCount,
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

fun LikeResponse.toDomain(): Like =
    Like(
        count = this.count,
        isLiked = this.isLiked,
    )

fun SearchHearitsResponse.toSearchedHearit(): PageResult<SearchedHearit> =
    PageResult(
        items = content.map { it.toSearchedHearit() },
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

fun HearitsResponse.toSearchedHearit(): PageResult<SearchedHearit> =
    PageResult(
        items = content.map { it.toSearchedHearit() },
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

fun HearitsResponse.toRecentUploadHearit(): PageResult<RecentUploadHearit> =
    PageResult(
        items = content.map { it.toRecentUploadHearit() },
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

fun RecommendationCategoriesResponse.toDomain(): RecommendationCategories =
    RecommendationCategories(
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        colorCode = this.colorCode,
        hearits = this.recommendationCategoryHearitResponses.map { it.toDomain() },
    )

fun RecommendationCategoryHearitResponse.toDomain(): CategoryHearit =
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
