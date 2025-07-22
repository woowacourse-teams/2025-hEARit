package com.onair.hearit.data.api

import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface MediaFileService {
    @GET("hearits/{hearitId}/short-audio-url")
    suspend fun getShortAudioUrl(
        @Path("hearitId") hearitId: Long,
    ): Response<ShortAudioUrlResponse>

    @GET("hearits/{hearitId}/script-url")
    suspend fun getScriptUrl(
        @Path("hearitId") hearitId: Long,
    ): Response<ScriptUrlResponse>

    @GET
    suspend fun getScriptJson(
        @Url url: String,
    ): Response<ResponseBody>
}
