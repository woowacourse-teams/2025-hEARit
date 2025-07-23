package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.RecentHearit

interface RecentHearitRepository {
    suspend fun getRecentHearit(): Result<RecentHearit>

    suspend fun saveRecentHearit(recentHearit: RecentHearit): Result<Unit>
}
