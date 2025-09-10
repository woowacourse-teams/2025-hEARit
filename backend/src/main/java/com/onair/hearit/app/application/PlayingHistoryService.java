package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.app.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayingHistoryService {

    private final PlayingHistoryRepository playingHistoryRepository;
    private final PlayingHistoryBuffer playingHistoryBuffer;
    private final HearitRepository hearitRepository;

    public boolean addPlayingHistory(UserContext userContext, PlayingHistoryRequest request) {
        if (userContext == null || userContext.isGuest()) {
            return false;
        }
        Hearit hearit = getHearitById(request.hearitId());
        PlayingHistory history = new PlayingHistory(userContext.memberId(), hearit, request.lastPlayTime());
        return !addPlayingHistory(history, request.hearitId(), userContext.memberId());
    }

    private boolean addPlayingHistory(PlayingHistory history, Long hearitId, Long memberId) {
        boolean isExited = playingHistoryRepository.existsByHearitIdAndMemberId(hearitId, memberId);
        playingHistoryBuffer.add(history);
        return isExited;
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
