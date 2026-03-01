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

    // 섹션별 로딩
    val isRecommendLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.RECOMMEND)
    val isRecentUploadLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.RECENT_UPLOAD)
    val isPlayingHistoryLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.PLAYING_HISTORY)
    val isBookmarkLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.PLAYING_BOOKMARKS)
    val isCategoriesLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.RECOMMENDATION_CATEGORIES)
    val isAdLoading: Boolean get() = loadingKeys.contains(HomeLoadKey.AD_BANNER)

    // 전역 로딩(인디케이터용)
    val isLoading: Boolean
        get() = loadingKeys.isNotEmpty()

    val showRecommendHearits: Boolean
        get() = !isRecommendLoading && recommendHearits.isNotEmpty()

    val showRecentUpload: Boolean
        get() = !isRecentUploadLoading && recentUploadHearits.isNotEmpty()

    val showPlayingHistory: Boolean
        get() = !isPlayingHistoryLoading && playingHistoryHearits.isNotEmpty()

    val showBookmark: Boolean
        get() = !isBookmarkLoading && playingBookmarkHearits.isNotEmpty()

    val showCategories: Boolean
        get() = !isCategoriesLoading && recommendationCategories.isNotEmpty()

    val showAdBanner: Boolean
        get() = !isAdLoading && advertisement != null
}
