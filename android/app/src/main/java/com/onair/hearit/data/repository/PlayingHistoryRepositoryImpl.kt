package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.PlayingHistoryDataSource
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.domain.repository.PlayingHistoryRepository

class PlayingHistoryRepositoryImpl(
    private val playingHistoryDataSource: PlayingHistoryDataSource,
) : PlayingHistoryRepository {
    override suspend fun addPlayingHistory(
        hearitId: Long,
        lastPlayTime: Long,
    ): Result<Unit> =
        runCatching {
            playingHistoryDataSource
                .addPlayingHistory(PlayingHistoryRequest(hearitId, lastPlayTime))
        }
}
