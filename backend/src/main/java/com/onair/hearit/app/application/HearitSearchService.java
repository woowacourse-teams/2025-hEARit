package com.onair.hearit.app.application;

import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.HearitSearchResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitSearchService {

    private static final int KEYWORD_PER_HEARIT = 3;

    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    public PagedResponse<HearitSearchResponse> search(String searchTerm, PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(searchTerm, pageable);
        Page<HearitSearchResponse> hearitDtos = hearits.map(this::toHearitSearchResponseWithKeywords);
        return PagedResponse.from(hearitDtos);
    }

    private HearitSearchResponse toHearitSearchResponseWithKeywords(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
                KEYWORD_PER_HEARIT);
        return HearitSearchResponse.from(hearit, keywords);
    }
}
