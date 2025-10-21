package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.PlayingHistoryDataSource
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import javax.inject.Inject

class PlayingHistoryRepositoryImpl @Inject constructor(
    private val playingHistoryDataSource: PlayingHistoryDataSource,
) : PlayingHistoryRepository {
    override suspend fun getPlayingHistories(): Result<List<PlayingHistoryHearit>> =
        playingHistoryDataSource.getPlayingHistories().mapListOrThrowDomain { it.toDomain() }

    override suspend fun addPlayingHistory(
        hearitId: Long,
        lastPlayTime: Long,
    ): Result<Unit> =
        runCatching {
            playingHistoryDataSource
                .addPlayingHistory(PlayingHistoryRequest(hearitId, lastPlayTime))
        }
}
