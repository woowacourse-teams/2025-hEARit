package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.app.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.UserInfo;
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

    public void addPlayingHistory(UserInfo userInfo, PlayingHistoryRequest request) {
        if (userInfo == null || userInfo.isGuest()) {
            return;
        }
        Hearit hearit = getHearitById(request.hearitId());
        PlayingHistory history = new PlayingHistory(userInfo.getMemberId(), hearit, request.lastPlayTime());
        playingHistoryBuffer.add(history);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
