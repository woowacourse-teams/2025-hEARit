package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.MediaFileRemoteDataSource
import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.RandomHearitItem
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortAudioUrl
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearitShortsItem
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

    override suspend fun getShortsHearitItem(item: RandomHearitItem): Result<HearitShorts> = combine(item)

    private suspend fun combine(item: RandomHearitItem): Result<HearitShorts> =
        handleResult {
            val hearitId = item.id

            val audioUrl =
                getShortAudioUrl(hearitId)
                    .getOrElse { throw it }
                    .url

            val scriptList =
                getSubtitleLines(hearitId)
                    .getOrElse { throw it }

            item.toHearitShortsItem(audioUrl, scriptList)
        }
}
