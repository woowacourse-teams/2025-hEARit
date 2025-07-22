package com.onair.hearit.domain.repository

import com.onair.hearit.domain.Hearit
import com.onair.hearit.domain.HearitShorts
import com.onair.hearit.domain.OriginalAudioUrl
import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.ScriptLine
import com.onair.hearit.domain.ShortAudioUrl
import com.onair.hearit.domain.SingleHearit

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrl>

    suspend fun getScriptLines(hearitId: Long): Result<List<ScriptLine>>

    suspend fun getOriginalAudioUrl(hearitId: Long): Result<OriginalAudioUrl>

    suspend fun getShortsHearitItem(item: RandomHearit): Result<HearitShorts>

    suspend fun getOriginalHearitItem(item: SingleHearit): Result<Hearit>
}
