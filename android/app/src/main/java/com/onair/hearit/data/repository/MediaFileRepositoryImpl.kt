package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.MediaFileRemoteDataSource
import com.onair.hearit.domain.Hearit
import com.onair.hearit.domain.HearitShorts
import com.onair.hearit.domain.OriginalAudioUrl
import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.ScriptLine
import com.onair.hearit.domain.ShortAudioUrl
import com.onair.hearit.domain.SingleHearit
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearit
import com.onair.hearit.domain.toHearitShorts
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

    override suspend fun getScriptLines(hearitId: Long): Result<List<ScriptLine>> =
        handleResult {
            val scriptUrl =
                mediaFileRemoteDataSource
                    .getScriptUrl(hearitId)
                    .getOrElse { throw it }
                    .url

            val responseBody =
                mediaFileRemoteDataSource
                    .getScriptJson(scriptUrl)
                    .getOrElse { throw it }

            val jsonString = responseBody.string()
            Json.Default.decodeFromString(jsonString)
        }

    override suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrl> =
        handleResult {
            val response =
                mediaFileRemoteDataSource
                    .getOriginalAudioUrl(hearitId)
                    .getOrElse { throw it }
            OriginalAudioUrl(id = response.id, url = response.url)
        }

    override suspend fun getShortsHearitItem(item: RandomHearit): Result<HearitShorts> = combineHearitShorts(item)

    override suspend fun getOriginalHearitItem(item: SingleHearit): Result<Hearit> = combineHearit(item)

    private suspend fun combineHearitShorts(item: RandomHearit): Result<HearitShorts> =
        handleResult {
            val hearitId = item.id

            val audioUrl =
                getShortAudioUrl(hearitId)
                    .getOrElse { throw it }
                    .url

            val scriptList =
                getScriptLines(hearitId)
                    .getOrElse { throw it }

            item.toHearitShorts(audioUrl, scriptList)
        }

    private suspend fun combineHearit(item: SingleHearit): Result<Hearit> =
        handleResult {
            val hearitId = item.id

            val audioUrl =
                getOriginalAudioUrl(hearitId)
                    .getOrElse { throw it }
                    .url

            val scriptList =
                getScriptLines(hearitId)
                    .getOrElse { throw it }

            item.toHearit(audioUrl, scriptList)
        }
}
