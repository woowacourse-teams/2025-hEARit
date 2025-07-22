package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.RandomHearitCondition;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int RECOMMEND_HEARIT_COUNT = 5;

    private final HearitRepository hearitRepository;
    private final BookmarkRepository bookmarkRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId, Long memberId) {
        Hearit hearit = getHearitById(hearitId);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearitId, memberId);
        if (bookmarkOptional.isPresent()) {
            return HearitDetailResponse.fromWithBookmark(hearit, bookmarkOptional.get());
        }
        return HearitDetailResponse.from(hearit);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    public List<RandomHearitResponse> getRandomHearits(Long memberId, RandomHearitCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> hearits = hearitRepository.findRandom(pageable);
        return hearits.stream()
                .map(hearit -> toRandomHearitResponse(hearit, memberId))
                .toList();
    }

    private RandomHearitResponse toRandomHearitResponse(Hearit hearit, Long memberId) {
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearit.getId(), memberId);
        if (bookmarkOptional.isPresent()) {
            return RandomHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get());
        }
        return RandomHearitResponse.from(hearit);
    }

    public List<RecommendHearitResponse> getRecommendedHearits() {
        Pageable pageable = PageRequest.of(0, RECOMMEND_HEARIT_COUNT);
        return hearitRepository.findRandom(pageable).stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }
}
