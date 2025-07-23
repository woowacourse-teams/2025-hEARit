package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.MediaFileRemoteDataSource
import com.onair.hearit.domain.ScriptLine
import com.onair.hearit.domain.ShortAudioUrl
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.serialization.json.Json

class MediaFileRepositoryImpl(
    private val mediaFileRemoteDataSource: MediaFileRemoteDataSource,
) : MediaFileRepository {
    override suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl> =
        handleResult {
            val response =
                mediaFileRemoteDataSource
                    .getShortAudioUrl(hearitId)
                    .getOrElse { throw it }
            ShortAudioUrl(id = response.id, url = response.url)
        }

    override suspend fun getSubtitleLines(hearitId: Long): Result<List<ScriptLine>> =
        handleResult {
            val scriptUrl =
                mediaFileRemoteDataSource.getScriptUrl(hearitId).getOrElse { throw it }.url
            val responseBody =
                mediaFileRemoteDataSource.getScriptJson(scriptUrl).getOrElse { throw it }
            val jsonString = responseBody.string()
            Json.decodeFromString(jsonString)
        }
}
