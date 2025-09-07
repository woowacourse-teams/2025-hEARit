package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.app.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
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
        validateHearit(request.hearitId());
        checkMember(userContext);
        playingHistoryBuffer.addPlayingHistory(userContext.memberId(), request.hearitId(), request.lastPlayTime());
        return !playingHistoryRepository.existsByHearitIdAndMemberId(request.hearitId(), userContext.memberId());
    }

    private void validateHearit(Long hearitId) {
        if (!hearitRepository.existsById(hearitId)) {
            throw new NotFoundException("hearitId", hearitId.toString());
        }
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
