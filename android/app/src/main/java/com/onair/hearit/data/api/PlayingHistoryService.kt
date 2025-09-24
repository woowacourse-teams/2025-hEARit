package com.onair.hearit.data.api

import com.onair.hearit.data.dto.PlayingHistoryRequest
import com.onair.hearit.data.dto.PlayingHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PlayingHistoryService {
    @GET("/api/v1/playing-histories/hearits")
    suspend fun getPlayingHistories(): Response<List<PlayingHistoryResponse>>

    @POST("api/v1/playing-histories")
    suspend fun postPlayingHistory(
        @Body playingHistoryRequest: PlayingHistoryRequest,
    ): Response<Unit>
}
