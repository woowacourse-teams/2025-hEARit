package com.onair.hearit.application;

import com.onair.hearit.dto.response.HearitExploreResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int MAX_EXPLORE_COUNT = 5;

    private final HearitRepository hearitRepository;

    public List<HearitExploreResponse> getExploredHearits() {
        Pageable pageable = PageRequest.of(0, MAX_EXPLORE_COUNT);

        return hearitRepository.findRandom(pageable).stream()
                .map(HearitExploreResponse::from)
                .toList();
    }
}
