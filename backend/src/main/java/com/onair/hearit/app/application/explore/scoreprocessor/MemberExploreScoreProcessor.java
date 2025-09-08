package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.auth.domain.UserType;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberExploreScoreProcessor implements ExploreScoreProcessor {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    private final MemberRepository memberRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public boolean isSupported(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            return false;
        }
        if (userContext.isMember() && memberRepository.existsById(userContext.getMemberId())) {
            return true;
        }
        return false;
    }

    @Override
    public List<ExploredHearitResponse> getExploreHearitsResponse(UserContext userContext, long cursorId, int size) {
        Member member = getMemberByUserContext(userContext);
        List<Hearit> exploredHearits = getExploredHearits(cursorId, member, size);
        return exploredHearits.stream()
                .map(hearit -> toExploredHearitResponse(hearit, member))
                .toList();
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

    private List<Hearit> getExploredHearits(Long cursorId, Member member, int size) {
        if (cursorId == 0L) {
            //TODO: 흐린눈 제거 userType 처리 어케할건지
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(member.getUuid(), UserType.MEMBER);
            exploreScoreCommandRepository.insertScores(member.getUuid(), scores);
            exploreScoreCommandRepository.updateCursorIds(member.getUuid());
        }
        return exploredHearitQueryRepository.findExploredHearits(member.getUuid(), cursorId, size);
    }

    private ExploredHearitResponse toExploredHearitResponse(Hearit hearit, Member member) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitAndMember(hearit, member);
        if (bookmarkOptional.isPresent()) {
            return ExploredHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords);
        }
        return ExploredHearitResponse.from(hearit, keywords);
    }
}
