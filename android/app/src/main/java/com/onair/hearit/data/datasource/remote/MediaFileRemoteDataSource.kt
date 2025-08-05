package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody

interface MediaFileRemoteDataSource {
    suspend fun getShortAudioUrl(hearitId: Long): Result<NetworkResult<ShortAudioUrlResponse>>

    suspend fun getScriptUrl(hearitId: Long): Result<NetworkResult<ScriptUrlResponse>>

    suspend fun getOriginalAudioUrl(hearitId: Long): Result<NetworkResult<OriginalAudioUrlResponse>>

    suspend fun getScriptJson(scriptUrl: String): Result<NetworkResult<ResponseBody>>
}
