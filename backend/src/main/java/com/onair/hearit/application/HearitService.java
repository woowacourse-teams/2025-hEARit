package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int MAX_EXPLORE_COUNT = 10;
    private static final int MAX_RECOMMEND_HEARIT_COUNT = 5;

    private final HearitRepository hearitRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId) {
        Hearit hearit = getHearitById(hearitId);
        return HearitDetailResponse.from(hearit);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    public List<HearitDetailResponse> getRandomHearits() {
        Pageable pageable = PageRequest.of(0, MAX_EXPLORE_COUNT);

        return hearitRepository.findRandom(pageable).stream()
                .map(HearitDetailResponse::from)
                .toList();
    }

    public List<HearitDetailResponse> getRecommendedHearits() {
        Pageable pageable = PageRequest.of(0, MAX_RECOMMEND_HEARIT_COUNT);

        return hearitRepository.findRandom(pageable).stream()
                .map(HearitDetailResponse::from)
                .toList();
    }
}
