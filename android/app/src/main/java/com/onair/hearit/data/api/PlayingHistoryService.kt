package com.onair.hearit.data.api

import com.onair.hearit.data.dto.PlayingHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PlayingHistoryService {
    @POST("playing-histories")
    suspend fun postPlayingHistory(
        @Body playingHistoryRequest: PlayingHistoryRequest,
    ): Response<Unit>
}
