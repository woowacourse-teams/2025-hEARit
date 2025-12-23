package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.repository.PlayingHistoryRepository
import java.time.Clock // java.time.Clock을 직접 사용
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hearit 재생 기록을 추가하는 UseCase
 *
 * @property playingHistoryRepository 재생 기록 관련 데이터 처리를 위한 Repository
 * @property clock 현재 시간을 얻기 위한 Clock 객체
 */
@Singleton
class AddPlayingHistoryUseCase @Inject constructor(
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val clock: Clock,
) {
    /**
     * 1초 이상 들은 겨우에만 재생 기록을 추가하는 함수
     *
     * @param hearitId 재생 기록을 추가할 Hearit의 ID
     * @param playedMs 사용자가 실제로 재생한 시간 (ms)
     */
    suspend operator fun invoke(
        hearitId: Long,
        playedMs: Long,
    ) {
        // 1초 이상 들은 경우에만 재생 기록을 저장할 수 있도록 함
        if (playedMs < 1_000L) return

        playingHistoryRepository.addPlayingHistory(
            hearitId = hearitId,
            lastPlayTime = playedMs,
            clientEventTime = clock.millis(),
        )
    }
}
