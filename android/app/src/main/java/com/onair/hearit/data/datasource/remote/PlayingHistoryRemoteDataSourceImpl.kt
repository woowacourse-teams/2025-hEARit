package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.datasource.handleApiCallUnit
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.dto.PlayingHistoryResponse
import javax.inject.Inject

class PlayingHistoryRemoteDataSourceImpl @Inject constructor(
    private val playingHistoryService: PlayingHistoryService,
    private val errorResponseHandler: ErrorResponseHandler,
) : PlayingHistoryRemoteDataSource {
    override suspend fun getPlayingHistories(): NetworkResult<List<PlayingHistoryResponse>> =
        handleApiCall(
            apiCall = { playingHistoryService.getPlayingHistories() },
            errorHandler = errorResponseHandler,
        )

    override suspend fun addPlayingHistory(playingHistoryRequest: PlayingHistoryRequest): NetworkResult<Unit> =
        handleApiCallUnit(
            apiCall = { playingHistoryService.postPlayingHistory(playingHistoryRequest) },
            errorHandler = errorResponseHandler,
        )
}
