package com.onair.hearit.domain.repository

import com.onair.hearit.domain.HearitShortsItem
import com.onair.hearit.domain.RandomHearitItem
import com.onair.hearit.domain.ShortAudioUrlItem
import com.onair.hearit.domain.SubtitleLine

interface MediaFileRepository {
    suspend fun getShortAudioUrl(hearitId: Long): Result<ShortAudioUrlItem>

    suspend fun getSubtitleLines(hearitId: Long): Result<List<SubtitleLine>>

    suspend fun getShortsHearitItem(item: RandomHearitItem): Result<HearitShortsItem>
}
