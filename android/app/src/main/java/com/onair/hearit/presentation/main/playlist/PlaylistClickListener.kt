package com.onair.hearit.presentation.main.playlist

import com.onair.hearit.domain.model.Bookmark

interface PlaylistClickListener {
    fun onClickHearit(hearitId: Long)

    fun onClickPlayToggle(item: Bookmark)
}
