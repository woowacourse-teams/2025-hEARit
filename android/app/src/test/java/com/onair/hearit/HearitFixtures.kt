package com.onair.hearit

import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.RecommendationCategoryHearitResponse
import com.onair.hearit.data.dto.SourceResponse

object HearitFixtures {
    fun createFakeHearit(hearitId: Long): HearitResponse =
        HearitResponse(
            id = hearitId,
            title = "테스트용 히어릿",
            summary = "요약",
            sources =
                listOf(
                    SourceResponse(sourceName = "출처1", sourceUrl = "출처링크1"),
                    SourceResponse(sourceName = "출처2", sourceUrl = "출처링크2"),
                ),
            playTime = 120,
            createdAt = "2025-08-05T12:00:00Z",
            isBookmarked = true,
            bookmarkId = 100L,
            category = CategoryResponse.Content(id = 1, colorCode = "#555555", name = "Kotlin"),
            keywords =
                listOf(
                    KeywordResponse(id = 1, name = "Activity"),
                ),
        )

    fun createFakeRecommendHearit(): RecommendHearitResponse =
        RecommendHearitResponse(
            id = 0,
            title = "테스트용 히어릿",
            playTime = 500,
            createdAt = "2025-08-06T12:00:00Z",
            categoryName = "Android",
            categoryColor = "purple",
        )

    fun createFakeRandomHearit(): RandomHearitResponse {
        val fakeContents =
            listOf(
                RandomHearitResponse.Content(
                    id = 1L,
                    title = "첫 번째 히어릿",
                    categoryColorCode = "#FF5733",
                    isBookmarked = false,
                    bookmarkId = null,
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드1"),
                            KeywordResponse(1, "키워드2"),
                        ),
                    cursorId = 0L,
                ),
                RandomHearitResponse.Content(
                    id = 2L,
                    title = "두 번째 히어릿",
                    categoryColorCode = "#33C1FF",
                    isBookmarked = true,
                    bookmarkId = 12345L,
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드3"),
                            KeywordResponse(1, "키워드4"),
                            KeywordResponse(2, "키워드5"),
                        ),
                    cursorId = 1L,
                ),
            )

        return RandomHearitResponse(
            content = fakeContents,
            isEmpty = false,
        )
    }

    fun createSearchHearit(): HearitsResponse {
        val fakeContents =
            listOf(
                HearitsResponse.Content(
                    id = 1L,
                    title = "첫 번째 test 히어릿",
                    playTime = 150,
                    lastPlayTime = 160000,
                    createdAt = "1234",
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드3"),
                            KeywordResponse(1, "키워드4"),
                            KeywordResponse(2, "키워드5"),
                        ),
                    category = CategoryResponse.Content(0L, "카테고리 1", "#123456"),
                ),
                HearitsResponse.Content(
                    id = 2L,
                    title = "두 번째 test 히어릿",
                    playTime = 210,
                    lastPlayTime = 222222,
                    createdAt = "1234",
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드3"),
                            KeywordResponse(1, "키워드4"),
                            KeywordResponse(2, "키워드5"),
                        ),
                    category = CategoryResponse.Content(0L, "카테고리 1", "#123456"),
                ),
            )

        return HearitsResponse(
            content = fakeContents,
            page = 0,
            size = fakeContents.size,
            totalPages = 5,
            totalElements = 10,
            isFirst = true,
            isLast = false,
        )
    }

    fun createGroupedCategory(): RecommendationCategoriesResponse {
        val fakeHearits =
            listOf(
                RecommendationCategoryHearitResponse(
                    createdAt = "2025-08-01T10:00:00Z",
                    hearitId = 1L,
                    title = "카테고리 히어릿 1",
                ),
                RecommendationCategoryHearitResponse(
                    createdAt = "2025-08-02T14:30:00Z",
                    hearitId = 2L,
                    title = "카테고리 히어릿 2",
                ),
            )

        return RecommendationCategoriesResponse(
            categoryId = 10L,
            categoryName = "Android",
            colorCode = "#FF9800",
            recommendationCategoryHearitResponses = fakeHearits,
        )
    }
}
