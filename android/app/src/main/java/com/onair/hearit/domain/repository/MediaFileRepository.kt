package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.OriginalAudioUrl
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortAudioUrl

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl>

    suspend fun getScriptLines(hearitId: Long): Result<List<ScriptLine>>

    suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrl>

    suspend fun getOriginalHearitItem(item: Hearit): Result<Hearit>
}
