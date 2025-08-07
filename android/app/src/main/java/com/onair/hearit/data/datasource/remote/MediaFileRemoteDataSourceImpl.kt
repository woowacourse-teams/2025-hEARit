package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody

class MediaFileRemoteDataSourceImpl(
    private val mediaFileService: MediaFileService,
    private val errorResponseHandler: ErrorResponseHandler,
) : MediaFileRemoteDataSource {
    override suspend fun getShortAudioUrl(hearitId: Long): Result<NetworkResult<ShortAudioUrlResponse>> =
        handleApiCall(
            apiCall = { mediaFileService.getShortAudioUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getScriptUrl(hearitId: Long): Result<NetworkResult<ScriptUrlResponse>> =
        handleApiCall(
            apiCall = { mediaFileService.getScriptUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getOriginalAudioUrl(hearitId: Long): Result<NetworkResult<OriginalAudioUrlResponse>> =
        handleApiCall(
            apiCall = { mediaFileService.getOriginalAudioUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getScriptJson(scriptUrl: String): Result<NetworkResult<ResponseBody>> =
        handleApiCall(
            apiCall = { mediaFileService.getScriptJson(scriptUrl) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
