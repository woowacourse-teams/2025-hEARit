package com.onair.hearit

import com.onair.hearit.data.dto.CategoryHearitResponse
import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.KeywordResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
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

    fun createFakeRandomHearit(): ExploreHearitResponse {
        val fakeContents =
            listOf(
                ExploreHearitResponse.Content(
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
                ExploreHearitResponse.Content(
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

        return ExploreHearitResponse(
            content = fakeContents,
            isEmpty = false,
        )
    }

    fun createSearchHearit(): SearchHearitResponse {
        val fakeContents =
            listOf(
                SearchHearitResponse.Content(
                    id = 1L,
                    playTime = 150,
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드3"),
                            KeywordResponse(1, "키워드4"),
                            KeywordResponse(2, "키워드5"),
                        ),
                    title = "첫 번째 test 히어릿",
                ),
                SearchHearitResponse.Content(
                    id = 2L,
                    playTime = 210,
                    keywords =
                        listOf(
                            KeywordResponse(0, "키워드3"),
                            KeywordResponse(1, "키워드4"),
                            KeywordResponse(2, "키워드5"),
                        ),
                    title = "두 번째 test 히어릿",
                ),
            )

        return SearchHearitResponse(
            content = fakeContents,
            page = 0,
            size = fakeContents.size,
            totalPages = 5,
            totalElements = 10,
            isFirst = true,
            isLast = false,
        )
    }

    fun createGroupedCategory(): GroupedCategoryHearitResponse {
        val fakeHearits =
            listOf(
                CategoryHearitResponse(
                    createdAt = "2025-08-01T10:00:00Z",
                    hearitId = 1L,
                    title = "카테고리 히어릿 1",
                ),
                CategoryHearitResponse(
                    createdAt = "2025-08-02T14:30:00Z",
                    hearitId = 2L,
                    title = "카테고리 히어릿 2",
                ),
            )

        return GroupedCategoryHearitResponse(
            categoryId = 10L,
            categoryName = "Android",
            colorCode = "#FF9800",
            categoryHearitResponses = fakeHearits,
        )
    }
}
