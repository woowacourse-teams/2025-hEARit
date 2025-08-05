package com.onair.hearit.data

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.dto.CategoryHearitResponse
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.domain.model.CategoryHearit
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
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

private fun SearchHearitResponse.Content.toDomain(): SearchedHearit =
    SearchedHearit(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        summary = this.summary,
    )

private fun RandomHearitResponse.Content.toDomain(): RandomHearit =
    RandomHearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        playTime = this.playTime,
        createdAt = this.createdAt,
        isBookmarked = this.isBookmarked,
        bookmarkId = this.bookmarkId,
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

fun RandomHearitResponse.toDomain(): PageResult<RandomHearit> =
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
        category = this.category,
        keywords = this.keywords.map { it.toDomain() },
    )

fun UserInfoResponse.toDomain(): UserInfo =
    UserInfo(
        id = this.id,
        nickname = this.nickname,
        profileImage = this.profileImage,
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
