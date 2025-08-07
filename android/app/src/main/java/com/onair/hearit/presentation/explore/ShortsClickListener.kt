package com.onair.hearit.presentation.explore

interface ShortsClickListener {
    fun onClickHearitInfo(hearitId: Long)

    fun onClickBookmark(
        hearitId: Long,
        callback: (bookmarkId: Long?) -> Unit,
    )
}
