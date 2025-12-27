package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.PlayingHistoryHearit

interface PlayingHistoryRepository {
    suspend fun getPlayingHistories(): Result<List<PlayingHistoryHearit>>

    suspend fun addPlayingHistory(
        hearitId: Long,
        lastPlayTime: Long,
        clientEventTime: Long,
    ): Result<Unit>
}
