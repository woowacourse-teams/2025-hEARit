package com.onair.hearit.service.model

import android.os.Bundle

data class LibraryPlayParams(
    val seedBookmarkId: Long,
    val seedHearitId: Long,
    val startPositionMs: Long,
) {
    companion object {
        const val EXTRA_SEED_BOOKMARK_ID = "SEED_BOOKMARK_ID"
        const val EXTRA_SEED_HEARIT_ID = "SEED_HEARIT_ID"
        const val EXTRA_START_POSITION_MS = "SEED_POSITION_MS"

        fun fromBundle(bundle: Bundle): LibraryPlayParams =
            LibraryPlayParams(
                seedBookmarkId = bundle.getLong(EXTRA_SEED_BOOKMARK_ID, -1L),
                seedHearitId = bundle.getLong(EXTRA_SEED_HEARIT_ID, -1L),
                startPositionMs = bundle.getLong(EXTRA_START_POSITION_MS, 0L),
            )
    }
}
