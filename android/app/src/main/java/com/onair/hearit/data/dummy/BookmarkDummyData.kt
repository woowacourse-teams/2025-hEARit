package com.onair.hearit.data.dummy

import com.onair.hearit.domain.model.Bookmark

object BookmarkDummyData {
    fun getBookmarks(): List<Bookmark> =
        listOf(
            Bookmark(
                hearitId = 1001L,
                bookmarkId = 1L,
                title = "HTTP와 HTTPS 차이",
                summary = "보안 프로토콜의 차이를 알아봅니다",
                playTime = 15,
            ),
            Bookmark(
                hearitId = 1002L,
                bookmarkId = 2L,
                title = "Intent란 무엇인가?",
                summary = "Intent의 역할과 사용법을 알아봅니다",
                playTime = 32,
            ),
            Bookmark(
                hearitId = 1003L,
                bookmarkId = 3L,
                title = "Context 이해하기",
                summary = "Context의 개념과 중요성을 설명합니다",
                playTime = 47,
            ),
            Bookmark(
                hearitId = 1004L,
                bookmarkId = 4L,
                title = "View와 ViewGroup 차이",
                summary = "두 UI 구성요소의 차이점에 대해 알아봅니다",
                playTime = 89,
            ),
            Bookmark(
                hearitId = 1005L,
                bookmarkId = 5L,
                title = "Jetpack Compose란?",
                summary = "Jetpack Compose의 개요를 설명합니다",
                playTime = 64,
            ),
            Bookmark(
                hearitId = 1006L,
                bookmarkId = 6L,
                title = "Fragment 생명주기",
                summary = "Fragment의 생명주기 흐름을 살펴봅니다",
                playTime = 108,
            ),
            Bookmark(
                hearitId = 1007L,
                bookmarkId = 7L,
                title = "코루틴 기초",
                summary = "Kotlin 코루틴의 기초 개념을 다룹니다",
                playTime = 56,
            ),
            Bookmark(
                hearitId = 1008L,
                bookmarkId = 8L,
                title = "DataBinding과 ViewBinding",
                summary = "두 바인딩 방식의 차이를 설명합니다",
                playTime = 73,
            ),
            Bookmark(
                hearitId = 1009L,
                bookmarkId = 9L,
                title = "LiveData vs StateFlow",
                summary = "두 데이터 흐름 방식의 차이를 비교합니다",
                playTime = 85,
            ),
            Bookmark(
                hearitId = 1010L,
                bookmarkId = 10L,
                title = "MVVM 패턴 이해하기",
                summary = "MVVM 아키텍처의 구조를 설명합니다",
                playTime = 102,
            ),
            Bookmark(
                hearitId = 1011L,
                bookmarkId = 11L,
                title = "Room 데이터베이스 소개",
                summary = "Room의 기본 개념과 사용법을 알아봅니다",
                playTime = 95,
            ),
            Bookmark(
                hearitId = 1012L,
                bookmarkId = 12L,
                title = "Navigation Component 활용",
                summary = "Navigation 컴포넌트의 실전 활용법",
                playTime = 112,
            ),
        )
}
