package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.infrastructure.dto.ExploredHearitProjection;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MemberExploreScoreProcessor extends AbstractExploreScoreProcessor {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public MemberExploreScoreProcessor(ExploredHearitQueryRepository exploredHearitQueryRepository,
                                       HearitKeywordRepository hearitKeywordRepository,
                                       MemberRepository memberRepository,
                                       BookmarkRepository bookmarkRepository) {
        super(exploredHearitQueryRepository, hearitKeywordRepository);
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
    protected String getUserUuid(UserInfo userInfo) {
        return getMemberById(userInfo.getMemberId()).getUuid();
    }

    @Override
    protected List<ExploredHearitResponse> convertToExploredHearitResponses(
            List<ExploredHearitProjection> exploredHearitProjections,
            UserInfo userInfo) {
        List<Hearit> hearits = exploredHearitProjections.stream()
                .map(ExploredHearitProjection::getHearit)
                .toList();
        Map<Hearit, List<Keyword>> keywordsMap = prepareKeywordsMap(hearits);
        Map<Long, Bookmark> bookmarksMap = prepareBookmarksMap(hearits, userInfo);

        return exploredHearitProjections.stream()
                .map(info -> {
                    Hearit hearit = info.getHearit();
                    List<Keyword> keywords = keywordsMap.getOrDefault(hearit, List.of());
                    Bookmark bookmark = bookmarksMap.get(hearit.getId());
                    return assembleExploredHearitResponse(info, bookmark, hearit, keywords);
                })
                .toList();
    }

    private Map<Long, Bookmark> prepareBookmarksMap(List<Hearit> hearits, UserInfo userInfo) {
        Member member = getMemberById(userInfo.getMemberId());
        return bookmarkRepository
                .findAllByHearitInAndMember(hearits, member).stream()
                .collect(Collectors.toMap(bookmark -> bookmark.getHearit().getId(), bookmark -> bookmark));
    }

    private ExploredHearitResponse assembleExploredHearitResponse(ExploredHearitProjection info, Bookmark bookmark,
                                                                  Hearit hearit, List<Keyword> keywords) {
        if (bookmark == null) {
            return ExploredHearitResponse.from(hearit, keywords, info.getCursorId());
        }
        return ExploredHearitResponse.fromWithBookmark(hearit, bookmark, keywords, info.getCursorId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
