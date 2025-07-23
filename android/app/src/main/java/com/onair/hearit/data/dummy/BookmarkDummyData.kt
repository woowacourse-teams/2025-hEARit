package com.onair.hearit.data.dummy

import com.onair.hearit.domain.model.Bookmark

object BookmarkDummyData {
    fun getBookmarks(): List<Bookmark> =
        listOf(
            Bookmark(id = 1L, title = "HTTP와 HTTPS 차이", playTime = 15),
            Bookmark(id = 2L, title = "Intent란 무엇인가?", playTime = 32),
            Bookmark(id = 3L, title = "Context 이해하기", playTime = 47),
            Bookmark(id = 4L, title = "View와 ViewGroup 차이", playTime = 89),
            Bookmark(id = 5L, title = "Jetpack Compose란?", playTime = 64),
            Bookmark(id = 6L, title = "Fragment 생명주기", playTime = 108),
            Bookmark(id = 7L, title = "코루틴 기초", playTime = 56),
            Bookmark(id = 8L, title = "DataBinding과 ViewBinding", playTime = 73),
            Bookmark(id = 9L, title = "LiveData vs StateFlow", playTime = 85),
            Bookmark(id = 10L, title = "MVVM 패턴 이해하기", playTime = 102),
            Bookmark(id = 11L, title = "Room 데이터베이스 소개", playTime = 95),
            Bookmark(id = 12L, title = "Navigation Component 활용", playTime = 112),
        )
}
