package com.onair.hearit.presentation.home

import com.onair.hearit.domain.model.Advertisement
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.model.isLoggedIn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeUiState(
    val userInfo: UserInfo = UserInfo.default(),
    val recommendHearits: ImmutableList<RecommendHearit> = persistentListOf(),
    val playingHistoryHearits: List<PlayingHistoryHearit> = emptyList(),
    val recentUploadHearits: List<RecentUploadHearit> = emptyList(),
    val playingBookmarkHearits: List<Bookmark> = emptyList(),
    val advertisement: Advertisement? = null,
    val recommendationCategories: List<RecommendationCategories> = emptyList(),
    val loadingKeys: Set<HomeLoadKey> = emptySet(),
) {
    val isLoggedIn: Boolean
        get() = userInfo.isLoggedIn()

    // 전역 로딩 (인디케이터용)
    val isLoading: Boolean
        get() = loadingKeys.isNotEmpty()

    val showRecommendHearits: Boolean
        get() = canShow(recommendHearits, HomeLoadKey.RECOMMEND)

    val showRecentUpload: Boolean
        get() = canShow(recentUploadHearits, HomeLoadKey.RECENT_UPLOAD)

    val showPlayingHistory: Boolean
        get() = canShow(playingHistoryHearits, HomeLoadKey.PLAYING_HISTORY)

    val showBookmark: Boolean
        get() = canShow(playingBookmarkHearits, HomeLoadKey.PLAYING_BOOKMARKS)

    val showCategories: Boolean
        get() = canShow(recommendationCategories, HomeLoadKey.RECOMMENDATION_CATEGORIES)

    private fun <T> canShow(
        list: List<T>,
        loadingKey: HomeLoadKey,
    ) = !loadingKeys.contains(loadingKey) && list.isNotEmpty()
}
