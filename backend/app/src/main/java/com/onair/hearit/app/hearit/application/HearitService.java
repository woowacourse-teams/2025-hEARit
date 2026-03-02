package com.onair.hearit.app.hearit.application;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse.LikeResponse;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.infrastructure.ViewCountRateLimiter;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.jpa.ReactionRepository;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private final BookmarkRepository bookmarkRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final ReactionRepository reactionRepository;
    private final ViewCountRateLimiter viewCountRateLimiter;

    @Transactional(readOnly = true)
    public HearitDetailResponse getHearitDetail(Long hearitId, UserInfo userInfo) {
        Hearit hearit = getHearitById(hearitId);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());
        long likeCount = reactionRepository.countByHearitAndType(hearit, ReactionType.LIKE);

        if (isGuest(userInfo)) {
            return HearitDetailResponse.ofGuest(hearit, keywords, likeCount);
        }

        UUID memberUuid = userInfo.getUuid();
        Long bookmarkId = findBookmarkId(hearit, memberUuid);
        Long lastPlayTime = calculateLastPlayTime(hearit, memberUuid);
        boolean isLiked = reactionRepository.existsByHearitAndUserUuidAndType(hearit, memberUuid, ReactionType.LIKE);

        LikeResponse like = new LikeResponse(likeCount, isLiked);
        return HearitDetailResponse.ofMember(hearit, keywords, lastPlayTime, bookmarkId, like);
    }

    private boolean isGuest(UserInfo userInfo) {
        return userInfo == null || userInfo.isGuest();
    }

    private Long findBookmarkId(Hearit hearit, UUID memberUuid) {
        return bookmarkRepository.findByHearitAndMemberUuid(hearit, memberUuid)
                .map(Bookmark::getId)
                .orElse(null);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private Long calculateLastPlayTime(Hearit hearit, UUID memberUuid) {
        Optional<Long> optionalLastPlayTime = playingHistoryRepository.findByHearitIdAndUserUuid(hearit.getId(),
                        memberUuid)
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

    @Transactional(readOnly = true)
    public PagedResponse<HearitOverviewResponse> getFilteredHearits(
            Long categoryId, HearitSortRequest sortRequest, UserInfo userInfo, PagingRequest pagingRequest) {
        UUID userUuid = userInfo.getUuid();
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

    public void increaseViewCount(Long hearitId, UserInfo userInfo) {
        if (!viewCountRateLimiter.tryAcquireViewKey(userInfo.getUuid(), hearitId)) {
            return;
        }

        int affectedRowCount = hearitRepository.increaseViewCount(hearitId);
        if (affectedRowCount == 0) {
            throw new NotFoundException("hearitId", hearitId.toString());
        }
    }
}
