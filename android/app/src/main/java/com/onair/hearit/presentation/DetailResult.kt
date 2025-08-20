package com.onair.hearit.presentation

import android.os.Bundle

sealed class DetailResult {
    data class Explore(
        val hearitId: Long,
        val bookmarkId: Long?,
    ) : DetailResult()

    data class Category(
        val bundle: Bundle,
    ) : DetailResult()

    data class Keyword(
        val bundle: Bundle,
    ) : DetailResult()
}
