package com.onair.hearit.data.api

import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface MediaFileService {
    @GET("api/v1/hearits/{hearitId}/short-audio-url")
    suspend fun getShortAudioUrl(
        @Path("hearitId") hearitId: Long,
    ): Response<ShortAudioUrlResponse>

    @GET("api/v1/hearits/{hearitId}/script-url")
    suspend fun getScriptUrl(
        @Path("hearitId") hearitId: Long,
    ): Response<ScriptUrlResponse>

    @GET("api/v1/hearits/{hearitId}/original-audio-url")
    suspend fun getOriginalAudioUrl(
        @Path("hearitId") hearitId: Long,
    ): Response<OriginalAudioUrlResponse>

    @GET
    suspend fun getScriptJson(
        @Url url: String,
        @Header("No-Auth") noAuth: Boolean = true,
    ): Response<ResponseBody>
}
