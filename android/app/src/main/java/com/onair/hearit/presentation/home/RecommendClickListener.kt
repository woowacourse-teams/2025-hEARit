package com.onair.hearit.presentation.home

interface RecommendClickListener {
    fun onClickRecommendHearit(
        hearitId: Long,
        title: String,
    )
}
