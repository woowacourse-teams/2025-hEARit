package com.onair.hearit.presentation.library

interface BookmarkClickListener {
    fun onClickOption(bookmarkId: Long)

    fun onClickBookmarkedHearit(hearitId: Long)
}
