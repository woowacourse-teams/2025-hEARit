package com.onair.hearit.data.dummy

import android.content.Context
import com.onair.hearit.R
import com.onair.hearit.domain.RecommendHearitItem

object RecommendDummyData {
    fun getRecommendData(context: Context): List<RecommendHearitItem> =
        listOf(
            RecommendHearitItem(
                1L,
                "추천 제목 1",
                context.getString(R.string.home_example_recommend_description),
            ),
            RecommendHearitItem(
                2L,
                "추천 제목 2",
                context.getString(R.string.home_example_recommend_description),
            ),
            RecommendHearitItem(
                3L,
                "추천 제목 3",
                context.getString(R.string.home_example_recommend_description),
            ),
            RecommendHearitItem(
                4L,
                "추천 제목 4",
                context.getString(R.string.home_example_recommend_description),
            ),
            RecommendHearitItem(
                5L,
                "추천 제목 5",
                context.getString(R.string.home_example_recommend_description),
            ),
        )
}
