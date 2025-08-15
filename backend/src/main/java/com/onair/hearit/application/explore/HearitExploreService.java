package com.onair.hearit.application.explore;

import com.onair.hearit.application.explore.score.BookmarkScoreFactor;
import com.onair.hearit.application.explore.score.RandomScoreFactor;
import com.onair.hearit.application.explore.score.RecencyScoreFactor;
import com.onair.hearit.application.explore.score.ScoreFactor;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.request.CursorRequest;
import com.onair.hearit.dto.request.CursorRequest;
import com.onair.hearit.dto.response.CursorResponse;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        List<Hearit> exploredHearits = getExploredHearitsForMember(cursorRequest.cursorId(), member.getId(), cursorRequest.size());
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
        return getMemberById(userContext.memberId());
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

    private List<Hearit> getExploredHearitsForMember(Long cursorId, Long memberId, int size) {
        if (isFirstExploreRequest(cursorId)) {
            List<Hearit> hearits = hearitRepository.findAll();
            List<ScoreFactor> scoreFactors = List.of(bookmarkScoreFactor, recencyScoreFactor, randomScoreFactor);
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(memberId, hearits, scoreFactors);
            exploreScoreCommandRepository.insertScores(memberId, scores);
            exploreScoreCommandRepository.updateCursorIds(memberId);
        }
        return exploredHearitQueryRepository.findExploredHearitsForMember(memberId, cursorId, size);
    }

    private List<Hearit> getExploredHearitsForGuest(Long cursorId, int size) {
        Long guestId = -1L;
        if (isFirstExploreRequest(cursorId)) {
            List<Hearit> hearits = hearitRepository.findAll();
            List<ScoreFactor> scoreFactors = List.of(recencyScoreFactor, randomScoreFactor);
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(guestId, hearits, scoreFactors);
            exploreScoreCommandRepository.insertScores(guestId, scores);
            exploreScoreCommandRepository.updateCursorIds(guestId);
        }
        return exploredHearitQueryRepository.findExploredHearitsForGuest(cursorId, size);
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
