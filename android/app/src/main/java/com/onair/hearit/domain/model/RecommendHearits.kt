package com.onair.hearit.domain.model

sealed class RecommendHearits {
    data object LeftNavigateItem : RecommendHearits()

    data object RightNavigateItem : RecommendHearits()

    data class Content(
        val hearit: RecommendHearit,
    ) : RecommendHearits()
}
