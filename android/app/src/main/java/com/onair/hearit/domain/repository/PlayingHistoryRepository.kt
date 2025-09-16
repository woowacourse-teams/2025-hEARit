package com.onair.hearit.domain.repository

interface PlayingHistoryRepository {
    suspend fun addPlayingHistory(
        hearitId: Long,
        lastPlayTime: Long,
    ): Result<Unit>
}
