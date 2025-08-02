package com.onair.hearit.application;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitSearchService {

    private static final int KEYWORD_SIZE_PER_HEARIT = 3;

    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    public PagedResponse<HearitSearchResponse> search(String searchTerm, PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(searchTerm, pageable);
        Page<HearitSearchResponse> hearitDtos = hearits.map(this::toHearitSearchResponse);
        return PagedResponse.from(hearitDtos);
    }

    private HearitSearchResponse toHearitSearchResponse(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId(),
                KEYWORD_SIZE_PER_HEARIT);
        return HearitSearchResponse.from(hearit, keywords);
    }
}
