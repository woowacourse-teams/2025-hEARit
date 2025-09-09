package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.infrastructure.dto.ExploredHearitInfo;
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
                                       MemberRepository memberRepository,
                                       BookmarkRepository bookmarkRepository) {
        super(exploreScoreCalculator, exploreScoreCommandRepository,
                exploredHearitQueryRepository, hearitKeywordRepository);
        this.memberRepository = memberRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Override
    public boolean isSupported(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            return false;
        }
        return userInfo.isMember() && memberRepository.existsById(userInfo.getMemberId());
    }

    @Override
    protected String getUserUuId(UserInfo userInfo) {
        return getMemberById(userInfo.getMemberId()).getUuid();
    }

    @Override
    protected ExploredHearitResponse toExploredHearitResponse(ExploredHearitInfo info, UserInfo userInfo) {
        Member member = getMemberById(userInfo.getMemberId());
        List<Keyword> keywords = getKeywords(info.getHearit());

        return bookmarkRepository.findByHearitAndMember(info.getHearit(), member)
                .map(bookmark ->
                        ExploredHearitResponse.fromWithBookmark(
                                info.getHearit(),
                                bookmark, keywords,
                                info.getCursorId()))
                .orElseGet(() -> ExploredHearitResponse.from(info.getHearit(), keywords, info.getCursorId()));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
