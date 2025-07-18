package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitPersonalDetailResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private final HearitRepository hearitRepository;
    private final BookmarkRepository bookmarkRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId) {
        Hearit hearit = getHearitById(hearitId);
        return HearitDetailResponse.from(hearit);
    }

    public HearitPersonalDetailResponse getHearitPersonalDetail(Long hearitId, Long memberId) {
        Hearit hearit = getHearitById(hearitId);
        Boolean isBookmarked = bookmarkRepository.existsByHearitIdAndMemberId(hearitId, memberId);
        return HearitPersonalDetailResponse.of(hearit, isBookmarked);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
