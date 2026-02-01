package com.onair.hearit.app.reaction.application;

import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactionService {

    private final HearitRepository hearitRepository;
    private final ReactionRepository reactionRepository;

    @Transactional
    public void addReaction(UserInfo userInfo, Long hearitId, ReactionType type) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new ForbiddenException("비회원은 좋아요를 추가할 권한이 없습니다.");
        }

        Hearit hearit = getHearitById(hearitId);
        if (isAlreadyExistReaction(userInfo, type, hearit)) {
            return;
        }
        saveReaction(userInfo, hearitId, type, hearit);
    }

    private boolean isAlreadyExistReaction(UserInfo userInfo, ReactionType type, Hearit hearit) {
        return reactionRepository.existsByHearitAndUserUuidAndType(hearit, userInfo.getUuid(), type);
    }

    private void saveReaction(UserInfo userInfo, Long hearitId, ReactionType type, Hearit hearit) {
        try {
            Reaction like = new Reaction(userInfo.getUuid(), hearit, type);
            reactionRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 리액션 요청이 동시성에 의해 발생함: user={}, hearit={}", userInfo.getUuid(), hearitId);
        }
    }

    @Transactional
    public void removeReaction(UserInfo userInfo, Long hearitId, ReactionType type) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new ForbiddenException("비회원은 좋아요를 삭제할 권한이 없습니다.");
        }
        Hearit hearit = getHearitById(hearitId);
        reactionRepository
                .findByUserUuidAndHearitAndType(userInfo.getUuid(), hearit, type)
                .ifPresent(reactionRepository::delete);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
