package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.HearitSearchResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitSearchService {

    private static final int KEYWORD_PER_HEARIT = 3;

    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final MemberRepository memberRepository;
    private final PlayingHistoryRepository playingHistoryRepository;

    public PagedResponse<HearitSearchResponse> search(String searchTerm, PagingRequest pagingRequest,
                                                      UserContext userContext) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(searchTerm, pageable);
        if (userContext == null || userContext.isGuest()) {
            return PagedResponse.from(hearits.map(this::toHearitSearchResponseForGuest));
        }

        Member member = getMemberByUserContext(userContext);
        return PagedResponse.from(hearits.map(hearit -> toHearitSearchResponseForMember(hearit, member)));
    }

    private HearitSearchResponse toHearitSearchResponseForGuest(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
                KEYWORD_PER_HEARIT);
        return HearitSearchResponse.of(hearit, keywords, null);
    }

    private HearitSearchResponse toHearitSearchResponseForMember(Hearit hearit, Member member) {
        Long lastPlayTime = playingHistoryRepository.findByHearitIdAndMemberId(hearit.getId(), member.getId())
                .map(PlayingHistory::getLastPlayTime)
                .orElse(null);
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
                KEYWORD_PER_HEARIT);
        return HearitSearchResponse.of(hearit, keywords, lastPlayTime);
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
}
