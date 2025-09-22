package com.onair.hearit.service.model

import android.os.Bundle

data class LibraryPlayParams(
    val seedBookmarkId: Long,
    val seedHearitId: Long,
) {
    companion object {
        const val EXTRA_SEED_BOOKMARK_ID = "SEED_BOOKMARK_ID"
        const val EXTRA_SEED_HEARIT_ID = "SEED_HEARIT_ID"

        fun fromBundle(bundle: Bundle): LibraryPlayParams =
            LibraryPlayParams(
                seedBookmarkId = bundle.getLong(EXTRA_SEED_BOOKMARK_ID, -1L),
                seedHearitId = bundle.getLong(EXTRA_SEED_HEARIT_ID, -1L),
            )
    }
}
