package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.RandomHearitItem
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortAudioUrl

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl>

    suspend fun getSubtitleLines(hearitId: Long): Result<List<ScriptLine>>

    suspend fun getShortsHearitItem(item: RandomHearitItem): Result<HearitShorts>
}
