package com.onair.hearit.domain.model

sealed class RecommendHearits {
    data class NavigateItem(
        val direction: Direction,
    ) : RecommendHearits()

    data class Content(
        val hearit: RecommendHearit,
    ) : RecommendHearits()
}
