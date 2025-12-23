package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody

interface MediaFileRemoteDataSource {
    suspend fun getShortAudioUrl(hearitId: Long): NetworkResult<ShortAudioUrlResponse>

    suspend fun getScriptUrl(hearitId: Long): NetworkResult<ScriptUrlResponse>

    suspend fun getOriginalAudioUrl(hearitId: Long): NetworkResult<OriginalAudioUrlResponse>

    suspend fun getScriptJson(scriptUrl: String): NetworkResult<ResponseBody>
}
