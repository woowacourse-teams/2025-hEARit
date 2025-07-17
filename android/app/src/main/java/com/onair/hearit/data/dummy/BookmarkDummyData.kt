package com.onair.hearit.data.dummy

import com.onair.hearit.domain.BookmarkItem

object BookmarkDummyData {
    fun getBookmarks(): List<BookmarkItem> =
        listOf(
            BookmarkItem(id = 1L, title = "HTTP와 HTTPS 차이", playTime = 15),
            BookmarkItem(id = 2L, title = "Intent란 무엇인가?", playTime = 32),
            BookmarkItem(id = 3L, title = "Context 이해하기", playTime = 47),
            BookmarkItem(id = 4L, title = "View와 ViewGroup 차이", playTime = 89),
            BookmarkItem(id = 5L, title = "Jetpack Compose란?", playTime = 64),
            BookmarkItem(id = 6L, title = "Fragment 생명주기", playTime = 108),
            BookmarkItem(id = 7L, title = "코루틴 기초", playTime = 56),
            BookmarkItem(id = 8L, title = "DataBinding과 ViewBinding", playTime = 73),
            BookmarkItem(id = 9L, title = "LiveData vs StateFlow", playTime = 85),
            BookmarkItem(id = 10L, title = "MVVM 패턴 이해하기", playTime = 102),
            BookmarkItem(id = 11L, title = "Room 데이터베이스 소개", playTime = 95),
            BookmarkItem(id = 12L, title = "Navigation Component 활용", playTime = 112),
        )
}
