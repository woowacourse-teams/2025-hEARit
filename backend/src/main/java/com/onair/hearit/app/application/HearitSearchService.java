package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.HearitSearchResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthenticatedException;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
                                                      UserInfo userInfo) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(toBooleanModeQuery(searchTerm), pageable);
        if (userInfo == null || userInfo.isGuest()) {
            return PagedResponse.from(hearits.map(this::toHearitSearchResponseForGuest));
        }

        Member member = getMemberByUserInfo(userInfo);
        return PagedResponse.from(hearits.map(hearit -> toHearitSearchResponseForMember(hearit, member)));
    }

    private String toBooleanModeQuery(String searchTerm) {
        return Arrays.stream(searchTerm.trim().split("\\s+"))
                .map(this::sanitizeToken)
                .filter(token -> token.length() >= 2)
                .map(token -> "+" + token + "*")
                .collect(Collectors.joining(" "));
    }

    private String sanitizeToken(String token) {
        return token.replaceAll("[+\\-~<>()\"*@]", "");
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

    private Member getMemberByUserInfo(UserInfo useruserInfo) {
        if (useruserInfo == null || useruserInfo.isGuest()) {
            throw new UnauthenticatedException();
        }
        return getMemberById(useruserInfo.getMemberId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
