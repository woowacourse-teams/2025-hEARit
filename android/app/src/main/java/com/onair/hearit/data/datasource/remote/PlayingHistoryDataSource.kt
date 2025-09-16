package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.PlayingHistoryRequest

interface PlayingHistoryDataSource {
    suspend fun addPlayingHistory(playingHistoryRequest: PlayingHistoryRequest): Result<NetworkResult<Unit>>
}
