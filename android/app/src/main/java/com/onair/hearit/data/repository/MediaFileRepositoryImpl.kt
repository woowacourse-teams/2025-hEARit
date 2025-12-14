package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.OriginalAudioUrl
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortAudioUrl
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MediaFileRepositoryImpl @Inject constructor(
    private val mediaFileRemoteDataSource: MediaFileRemoteDataSource,
) : MediaFileRepository {
    override suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl> =
        mediaFileRemoteDataSource
            .getShortAudioUrl(hearitId)
            .toDomainResult { response ->
                ShortAudioUrl(
                    id = response.id,
                    url = response.url,
                )
            }

    override suspend fun getScriptLines(hearitId: Long): Result<List<ScriptLine>> =
        mediaFileRemoteDataSource
            .getScriptUrl(hearitId)
            .toDomainResult { it.url }
            .flatMap { scriptUrl ->
                mediaFileRemoteDataSource
                    .getScriptJson(scriptUrl)
                    .toDomainResult { responseBody ->
                        responseBody.use { body ->
                            val jsonString = body.string()
                            Json.decodeFromString(jsonString)
                        }
                    }
            }

    override suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrl> =
        mediaFileRemoteDataSource
            .getOriginalAudioUrl(hearitId)
            .toDomainResult { response -> OriginalAudioUrl(id = response.id, url = response.url) }

    override suspend fun getOriginalHearitItem(item: Hearit): Result<Hearit> = combineHearit(item)

    private suspend fun combineHearit(item: Hearit): Result<Hearit> =
        getOriginalAudioUrl(item.id)
            .mapCatching {
                it.url
            }.flatMap { audioUrl ->
                getScriptLines(item.id).mapCatching { scriptLines ->
                    item.copy(audioUrl = audioUrl, script = scriptLines)
                }
            }

    private inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
        fold(onSuccess = { transform(it) }, onFailure = { Result.failure(it) })
}
