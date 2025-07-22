package com.onair.hearit.data

import okhttp3.ResponseBody

interface MediaFileRemoteDataSource {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrlResponse>

    suspend fun getScriptUrl(hearitId: Long): Result<ScriptUrlResponse>

    suspend fun getScriptJson(scriptUrl: String): Result<ResponseBody>
}
