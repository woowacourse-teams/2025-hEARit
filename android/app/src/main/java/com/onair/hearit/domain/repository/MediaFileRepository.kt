package com.onair.hearit.domain.repository

import RandomHearitItem
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.OriginalAudioUrl
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.domain.model.ShortAudioUrl
import com.onair.hearit.domain.model.SingleHearit

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl>

    suspend fun getScriptLines(hearitId: Long): Result<List<ScriptLine>>

    suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrl>

    suspend fun getShortsHearitItem(item: RandomHearitItem): Result<HearitShorts>

    suspend fun getOriginalHearitItem(item: SingleHearit): Result<Hearit>
}
