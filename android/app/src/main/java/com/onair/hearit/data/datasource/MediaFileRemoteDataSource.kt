package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody

interface MediaFileRemoteDataSource {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrlResponse>

    suspend fun getScriptUrl(hearitId: Long): Result<ScriptUrlResponse>

    suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrlResponse>

    suspend fun getScriptJson(scriptUrl: String): Result<ResponseBody>
}
