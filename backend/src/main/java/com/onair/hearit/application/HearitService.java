package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int MAX_EXPLORE_COUNT = 10;
    private static final int MAX_TODAY_HEARIT_COUNT = 4;
    private static final long TODAY_RECOMMEND_HEARIT = 1L;

    private final HearitRepository hearitRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId) {
        Hearit hearit = getHearitById(hearitId);
        return HearitDetailResponse.from(hearit);
    }

    public List<HearitDetailResponse> getExploredHearits() {
        Pageable pageable = PageRequest.of(0, MAX_EXPLORE_COUNT);

        return hearitRepository.findRandom(pageable).stream()
                .map(HearitDetailResponse::from)
                .toList();
    }

    public List<HearitDetailResponse> getTodayHearits() {
        Pageable pageable = PageRequest.of(0, MAX_TODAY_HEARIT_COUNT);

        List<Hearit> hearits = new ArrayList<>();
        hearits.add(getHearitById(TODAY_RECOMMEND_HEARIT));
        hearits.addAll(hearitRepository.findRandom(pageable));

        return hearits.stream()
                .map(HearitDetailResponse::from)
                .toList();
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
