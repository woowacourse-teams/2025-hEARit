package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.dto.PlayingHistoryResponse
import javax.inject.Inject

class PlayingHistoryDataSourceImpl @Inject constructor(
    private val playingHistoryService: PlayingHistoryService,
    private val errorResponseHandler: ErrorResponseHandler,
) : PlayingHistoryDataSource {
    override suspend fun getPlayingHistories(): Result<NetworkResult<List<PlayingHistoryResponse>>> =
        handleApiCall(
            apiCall = { playingHistoryService.getPlayingHistories() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun addPlayingHistory(playingHistoryRequest: PlayingHistoryRequest): Result<NetworkResult<Unit>> =
        handleApiCall(
            apiCall = { playingHistoryService.postPlayingHistory(playingHistoryRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
