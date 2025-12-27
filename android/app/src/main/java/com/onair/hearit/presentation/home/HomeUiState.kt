package com.onair.hearit.presentation.home

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
    val recommendationCategories: List<RecommendationCategories> = emptyList(),
    val loadingKeys: Set<HomeLoadKey> = emptySet(),
) {
    val showRecommendHearits: Boolean
        get() = !isLoading && recommendHearits.isNotEmpty()

    val isLoggedIn: Boolean
        get() = userInfo.isLoggedIn()

    val showRecentUpload: Boolean
        get() = !isLoading && recentUploadHearits.isNotEmpty()

    val showPlayingHistory: Boolean
        get() = !isLoading && playingHistoryHearits.isNotEmpty()

    val showBookmark: Boolean
        get() = !isLoading && playingBookmarkHearits.isNotEmpty()

    val showCategories: Boolean
        get() = !isLoading && recommendationCategories.isNotEmpty()

    val isLoading: Boolean
        get() = loadingKeys.isNotEmpty()
}
