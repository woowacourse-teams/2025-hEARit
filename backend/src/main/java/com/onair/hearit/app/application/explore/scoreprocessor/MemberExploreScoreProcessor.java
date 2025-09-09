package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MemberExploreScoreProcessor extends AbstractExploreScoreProcessor {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public MemberExploreScoreProcessor(ExploreScoreCalculator exploreScoreCalculator,
                                       ExploreScoreCommandRepository exploreScoreCommandRepository,
                                       ExploredHearitQueryRepository exploredHearitQueryRepository,
                                       HearitKeywordRepository hearitKeywordRepository,
                                       MemberRepository memberRepository, BookmarkRepository bookmarkRepository) {
        super(exploreScoreCalculator, exploreScoreCommandRepository,
                exploredHearitQueryRepository, hearitKeywordRepository);
        this.memberRepository = memberRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Override
    public boolean isSupported(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            return false;
        }
        return userContext.isMember() && memberRepository.existsById(userContext.getMemberId());
    }

    @Override
    protected String getUserUuId(UserContext userContext) {
        return getMemberById(userContext.getMemberId()).getUuid();
    }

    @Override
    protected ExploredHearitResponse toExploredHearitResponse(Hearit hearit, UserContext userContext) {
        Member member = getMemberById(userContext.getMemberId());
        List<Keyword> keywords = getKeywords(hearit);

        return bookmarkRepository.findByHearitAndMember(hearit, member)
                .map(bookmark -> ExploredHearitResponse.fromWithBookmark(hearit, bookmark, keywords))
                .orElseGet(() -> ExploredHearitResponse.from(hearit, keywords));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
