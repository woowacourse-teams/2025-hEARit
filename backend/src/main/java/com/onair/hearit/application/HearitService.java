package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private final HearitRepository hearitRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId) {
        Hearit hearit = getHearitById(hearitId);
        return HearitDetailResponse.from(hearit);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
