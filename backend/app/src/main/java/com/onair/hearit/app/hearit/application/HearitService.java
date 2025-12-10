package com.onair.hearit.app.hearit.application;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.exception.custom.UnauthenticatedException;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.userinfo.application.UserInfoService;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int KEYWORDS_PER_CATEGORIZED_HEARIT = 3;

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final UserInfoService userInfoService;

    @Transactional(readOnly = true)
    public HearitDetailResponse getHearitDetail(Long hearitId, UserInfo userInfo) {
        Hearit hearit = getHearitById(hearitId);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());
        if (userInfo == null || userInfo.isGuest()) {
            return HearitDetailResponse.of(hearit, keywords, null, null);
        }

        Member member = getMemberByUserInfo(userInfo);
        Long bookmarkId = bookmarkRepository.findByHearitAndMember(hearit, member)
                .map(Bookmark::getId)
                .orElse(null);
        Long lastPlayTime = calculateLastPlayTime(hearit, member);
        return HearitDetailResponse.of(hearit, keywords, lastPlayTime, bookmarkId);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private Member getMemberByUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new UnauthenticatedException();
        }
        return getMemberById(userInfo.getMemberId());
    }

    private Long calculateLastPlayTime(Hearit hearit, Member member) {
        Optional<Long> optionalLastPlayTime = playingHistoryRepository.findByHearitIdAndUserUuid(hearit.getId(),
                        member.getUuid())
                .map(PlayingHistory::getLastPlayTime);
        if (optionalLastPlayTime.isEmpty()) {
            return null;
        }
        Long lastPlayTime = optionalLastPlayTime.get();
        Long remainingMillis = hearit.getPlayTime() * 1000 - lastPlayTime; // playTime(s), lastPlayTime(ms)
        if (remainingMillis <= 5000) {
            return 0L;
        }
        return lastPlayTime;
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }

    @Transactional(readOnly = true)
    public PagedResponse<HearitOverviewResponse> getFilteredHearits(
            Long categoryId, HearitSortRequest sortRequest, UserInfo userInfo, PagingRequest pagingRequest) {
        String userUuid = userInfoService.getUuid(userInfo);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), sortRequest.toSort());
        Page<HearitWithPlayTimeProjection> hearitsWithPlayTime = hearitRepository.findWithPlayTimeBy(
                categoryId,
                userUuid,
                pageable
        );
        List<Long> hearitIds = hearitsWithPlayTime.stream()
                .map(HearitWithPlayTimeProjection::getHearit)
                .map(Hearit::getId)
                .toList();
        Map<Long, List<Keyword>> keywordMap = getKeywordsMap(hearitIds);
        Page<HearitOverviewResponse> response = mapToFilteredHearits(hearitsWithPlayTime, keywordMap);
        return PagedResponse.from(response);
    }

    private Map<Long, List<Keyword>> getKeywordsMap(List<Long> hearitIds) {
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);
        return hearitKeywords.stream()
                .collect(Collectors.groupingBy(
                        hk -> hk.getHearit().getId(),
                        Collectors.mapping(HearitKeyword::getKeyword, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(KEYWORDS_PER_CATEGORIZED_HEARIT)
                                .toList()
                ));
    }

    private static Page<HearitOverviewResponse> mapToFilteredHearits(
            Page<HearitWithPlayTimeProjection> hearitsWithPlayTime,
            Map<Long, List<Keyword>> keywordMap) {
        return hearitsWithPlayTime.map(projection -> {
            Hearit hearit = projection.getHearit();
            Long lastPlayTime = projection.getLastPlayTime();
            List<Keyword> keywords = keywordMap.getOrDefault(hearit.getId(), Collections.emptyList());
            return HearitOverviewResponse.from(hearit, keywords, lastPlayTime);
        });
    }
}
