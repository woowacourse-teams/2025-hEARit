package com.onair.hearit.domain.repository

import com.onair.hearit.domain.ScriptLine
import com.onair.hearit.domain.ShortAudioUrl

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl>

    suspend fun getSubtitleLines(hearitId: Long): Result<List<ScriptLine>>
}
