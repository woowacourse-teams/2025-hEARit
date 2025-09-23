package com.onair.hearit.presentation

import android.os.Bundle
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY

sealed class DetailResult {
    data class Category(
        val categoryId: Long,
        val name: String,
        val colorCode: String,
    ) : DetailResult() {
        companion object {
            fun fromBundle(bundle: Bundle): Category =
                Category(
                    categoryId = bundle.getLong(CATEGORY_ID_KEY),
                    name = bundle.getString(CATEGORY_NAME_KEY) ?: DEFAULT_CATEGORY_NAME,
                    colorCode = bundle.getString(CATEGORY_COLOR_KEY) ?: DEFAULT_CATEGORY_COLOR,
                )
        }
    }

    data class Keyword(
        val term: String,
    ) : DetailResult() {
        companion object {
            fun fromBundle(bundle: Bundle): Keyword =
                Keyword(
                    term = bundle.getString(KEYWORD_KEY) ?: DEFAULT_KEYWORD_NAME,
                )
        }
    }

    companion object {
        private const val DEFAULT_KEYWORD_NAME = "hEARit"
        private const val DEFAULT_CATEGORY_NAME = "hEARit"
        private const val DEFAULT_CATEGORY_COLOR = "#000000"
    }
}
