package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.PlayingHistoryRemoteDataSource
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.data.toDomainResultList
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.repository.PlayingHistoryRepository

class PlayingHistoryRepositoryImpl(
    private val playingHistoryRemoteDataSource: PlayingHistoryRemoteDataSource,
) : PlayingHistoryRepository {
    override suspend fun getPlayingHistories(): Result<List<PlayingHistoryHearit>> =
        playingHistoryRemoteDataSource.getPlayingHistories().toDomainResultList { it.toDomain() }

    override suspend fun addPlayingHistory(
        hearitId: Long,
        lastPlayTime: Long,
    ): Result<Unit> =
        playingHistoryRemoteDataSource
            .addPlayingHistory(PlayingHistoryRequest(hearitId, lastPlayTime))
            .toDomainResult()
}
