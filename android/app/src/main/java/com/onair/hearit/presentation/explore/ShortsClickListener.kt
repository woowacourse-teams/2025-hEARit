package com.onair.hearit.presentation.explore

interface ShortsClickListener {
    fun onClickHearitInfo(
        hearitId: Long,
        title: String,
    )

    fun onClickBookmark(
        hearitId: Long,
        callback: (bookmarkId: Long?) -> Unit,
    )
}
