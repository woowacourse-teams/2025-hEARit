package com.onair.hearit.app.application;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.application.explore.score.BookmarkScoreFactor;
import com.onair.hearit.app.application.explore.score.RandomScoreFactor;
import com.onair.hearit.app.application.explore.score.RecencyScoreFactor;
import com.onair.hearit.app.application.explore.score.ScoreFactor;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.app.dto.request.CursorRequest;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    private final MemberRepository memberRepository;
    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final BookmarkRepository bookmarkRepository;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final BookmarkScoreFactor bookmarkScoreFactor;
    private final RecencyScoreFactor recencyScoreFactor;
    private final RandomScoreFactor randomScoreFactor;

    public CursorResponse<ExploredHearitResponse> getExploredHearits(UserContext userContext, CursorRequest cursorRequest) {
        if (userContext == null || userContext.isGuest()) {
            List<Hearit> exploredHearits = getExploredHearitsForGuest(cursorRequest.cursorId(), cursorRequest.size());
            List<ExploredHearitResponse> exploredHearitsDto = exploredHearits.stream()
                    .map(this::toExploredHearitResponse)
                    .toList();
            long updatedCursorId = cursorRequest.cursorId() + exploredHearits.size();
            return CursorResponse.from(exploredHearitsDto, updatedCursorId);
        }

        Member member = getMemberByUserContext(userContext);
        List<Hearit> exploredHearits = getExploredHearitsForMember(cursorRequest.cursorId(), member, cursorRequest.size());
        List<ExploredHearitResponse> exploredHearitsDto = exploredHearits.stream()
                .map(hearit -> toExploredHearitResponseWithBookmark(hearit, member))
                .toList();
        long updatedCursorId = cursorRequest.cursorId() + exploredHearits.size();
        return CursorResponse.from(exploredHearitsDto, updatedCursorId);
    }

    private Member getMemberByUserContext(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            throw new UnauthorizedException("로그인한 회원이 아닙니다.");
        }
        return getMemberById(userContext.getMemberId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }

    private ExploredHearitResponse toExploredHearitResponseWithBookmark(Hearit hearit, Member member) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);

        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitAndMember(hearit, member);
        if (bookmarkOptional.isPresent()) {
            return ExploredHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords);
        }
        return ExploredHearitResponse.from(hearit, keywords);
    }

    private List<Hearit> getExploredHearitsForMember(Long cursorId, Member member, int size) {
        if (isFirstExploreRequest(cursorId)) {
            List<Hearit> hearits = hearitRepository.findAll();
            List<ScoreFactor> scoreFactors = List.of(bookmarkScoreFactor, recencyScoreFactor, randomScoreFactor);
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(member.getId(), hearits, scoreFactors);
            exploreScoreCommandRepository.insertScores(member.getUuid(), scores);
            exploreScoreCommandRepository.updateCursorIds(member.getUuid());
        }
        return exploredHearitQueryRepository.findExploredHearits(member.getUuid(), cursorId, size);
    }

    private List<Hearit> getExploredHearitsForGuest(Long cursorId, int size) {
        //TODO: 임시 비회원 UUID 랜덤처리
        String guestId = UUID.randomUUID().toString();

        if (isFirstExploreRequest(cursorId)) {
            List<Hearit> hearits = hearitRepository.findAll();
            List<ScoreFactor> scoreFactors = List.of(recencyScoreFactor, randomScoreFactor);
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(-1L, hearits, scoreFactors); //FIXME: -1L 하드코딩 수정
            exploreScoreCommandRepository.insertScores(guestId, scores);
            exploreScoreCommandRepository.updateCursorIds(guestId);
        }
        return exploredHearitQueryRepository.findExploredHearits(guestId, cursorId, size);
    }

    private boolean isFirstExploreRequest(Long cursorId) {
        return cursorId == 0;
    }

    private ExploredHearitResponse toExploredHearitResponse(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);
        return ExploredHearitResponse.from(hearit, keywords);
    }
}
