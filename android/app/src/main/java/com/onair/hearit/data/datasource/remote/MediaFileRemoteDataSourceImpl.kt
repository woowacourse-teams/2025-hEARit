package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody
import javax.inject.Inject

class MediaFileRemoteDataSourceImpl @Inject constructor(
    private val mediaFileService: MediaFileService,
    private val errorResponseHandler: ErrorResponseHandler,
) : MediaFileRemoteDataSource {
    override suspend fun getShortAudioUrl(hearitId: Long): NetworkResult<ShortAudioUrlResponse> =
        handleApiCall(
            apiCall = { mediaFileService.getShortAudioUrl(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getScriptUrl(hearitId: Long): NetworkResult<ScriptUrlResponse> =
        handleApiCall(
            apiCall = { mediaFileService.getScriptUrl(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getOriginalAudioUrl(hearitId: Long): NetworkResult<OriginalAudioUrlResponse> =
        handleApiCall(
            apiCall = { mediaFileService.getOriginalAudioUrl(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getScriptJson(scriptUrl: String): NetworkResult<ResponseBody> =
        handleApiCall(
            apiCall = { mediaFileService.getScriptJson(scriptUrl) },
            errorHandler = errorResponseHandler,
        )
}
