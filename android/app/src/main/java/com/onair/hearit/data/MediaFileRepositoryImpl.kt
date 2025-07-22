package com.onair.hearit.data

import com.onair.hearit.domain.HearitShortsItem
import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.ShortAudioUrlItem
import com.onair.hearit.domain.SubtitleLine
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearitShortsItem
import kotlinx.serialization.json.Json

class MediaFileRepositoryImpl(
    private val mediaFileRemoteDataSource: MediaFileRemoteDataSource,
) : MediaFileRepository {
    override suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrlItem> =
        handleResult {
            val response =
                mediaFileRemoteDataSource
                    .getShortAudioUrl(hearitId)
                    .getOrElse { throw it }
            ShortAudioUrlItem(id = response.id, url = response.url)
        }

    override suspend fun getSubtitleLines(hearitId: Long): Result<List<SubtitleLine>> =
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
            Json.decodeFromString(jsonString)
        }

    override suspend fun getShortsHearitItem(item: RandomHearitItem): Result<HearitShortsItem> = combine(item)

    private suspend fun combine(item: RandomHearitItem): Result<HearitShortsItem> =
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
