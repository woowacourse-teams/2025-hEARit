package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayingHistoryService {

    private final PlayingHistoryRepository playingHistoryRepository;
    private final HearitRepository hearitRepository;

    @Transactional
    public void addPlayingHistory(UserContext userContext, PlayingHistoryRequest request) {
        checkMember(userContext);
        Hearit hearit = getHearitById(request.hearitId());
        PlayingHistory playingHistory = new PlayingHistory(userContext.memberId(), hearit, request.lastPlayTime());
        playingHistoryRepository.save(playingHistory);
    }

    private void checkMember(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            throw new UnauthorizedException("로그인한 회원이 아닙니다.");
        }
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
