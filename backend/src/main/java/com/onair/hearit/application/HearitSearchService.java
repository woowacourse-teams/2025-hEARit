package com.onair.hearit.application;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.TitleSearchRequest;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HearitSearchService {

    private final HearitRepository hearitRepository;

    public HearitSearchService(HearitRepository hearitRepository) {
        this.hearitRepository = hearitRepository;
    }

    public List<HearitSearchResponse> searchByTitle(TitleSearchRequest condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> pageResult = hearitRepository.findByTitleOrderByCreatedAtDesc(condition.search(), pageable);
        return HearitSearchResponse.from(pageResult);
    }
}