package com.onair.hearit.app.like.application;

import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeService {

    private final HearitRepository hearitRepository;
    private final ReactionRepository reactionRepository;

    @Transactional
    public void addLike(UserInfo userInfo, Long hearitId) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new ForbiddenException("비회원은 좋아요을 추가할 권한이 없습니다.");
        }
        Hearit hearit = getHearitById(hearitId);
        Reaction like = new Reaction(userInfo.getUuid(), hearit, ReactionType.LIKE);
        reactionRepository.save(like);
    }

    @Transactional
    public void removeLike(UserInfo userInfo, Long hearitId) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new ForbiddenException("비회원은 좋아요을 삭제할 권한이 없습니다.");
        }
        Hearit hearit = getHearitById(hearitId);
        reactionRepository
                .findByUserUuidAndHearit(userInfo.getUuid(), hearit)
                .ifPresent(reactionRepository::delete);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
