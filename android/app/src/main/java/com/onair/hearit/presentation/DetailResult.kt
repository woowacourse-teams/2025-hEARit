package com.onair.hearit.presentation

import android.os.Bundle
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY

sealed class DetailResult {
    data class Explore(
        val hearitId: Long,
        val bookmarkId: Long?,
    ) : DetailResult()

    data class Category(
        val id: Long,
        val name: String,
        val colorCode: String,
    ) : DetailResult() {
        constructor(bundle: Bundle) : this(
            id = bundle.getLong(CATEGORY_ID_KEY),
            name = bundle.getString(CATEGORY_NAME_KEY) ?: "hEARit",
            colorCode = bundle.getString(CATEGORY_COLOR_KEY) ?: "#000000",
        )
    }

    data class Keyword(
        val bundle: Bundle,
    ) : DetailResult()
}
