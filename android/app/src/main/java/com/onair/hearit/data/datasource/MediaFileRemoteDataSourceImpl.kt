package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.dto.OriginalAudioUrlResponse
import com.onair.hearit.data.dto.ScriptUrlResponse
import com.onair.hearit.data.dto.ShortAudioUrlResponse
import okhttp3.ResponseBody

class MediaFileRemoteDataSourceImpl(
    private val mediaFileService: MediaFileService,
) : MediaFileRemoteDataSource {
    override suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrlResponse> =
        handleApiCall(
            errorMessage = ERROR_AUDIO_FILE_LOAD_MESSAGE,
            apiCall = { mediaFileService.getShortAudioUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getScriptUrl(hearitId: Long): Result<ScriptUrlResponse> =
        handleApiCall(
            errorMessage = ERROR_SCRIPT_FILE_LOAD_MESSAGE,
            apiCall = { mediaFileService.getScriptUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrlResponse> =
        handleApiCall(
            errorMessage = ERROR_AUDIO_FILE_LOAD_MESSAGE,
            apiCall = { mediaFileService.getOriginalAudioUrl(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getScriptJson(scriptUrl: String): Result<ResponseBody> =
        runCatching {
            val response = mediaFileService.getScriptJson(scriptUrl)

            if (!response.isSuccessful) {
                throw Exception("ERROR_SCRIPT_JSON_REQUEST_MESSAGE ${response.code()} ${response.message()}")
            }

            response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(ERROR_SCRIPT_JSON_REQUEST_MESSAGE, it)) },
        )

    companion object {
        private const val ERROR_AUDIO_FILE_LOAD_MESSAGE = "오디오 파일 조회 실패"
        private const val ERROR_SCRIPT_FILE_LOAD_MESSAGE = "스크립트 파일 조회 실패"
        private const val ERROR_SCRIPT_JSON_REQUEST_MESSAGE = "스크립트 JSON 요청 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
    }
}
