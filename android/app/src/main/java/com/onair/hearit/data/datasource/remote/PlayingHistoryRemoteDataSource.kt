package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.dto.PlayingHistoryResponse

interface PlayingHistoryRemoteDataSource {
    suspend fun getPlayingHistories(): NetworkResult<List<PlayingHistoryResponse>>

    suspend fun addPlayingHistory(playingHistoryRequest: PlayingHistoryRequest): NetworkResult<Unit>
}
