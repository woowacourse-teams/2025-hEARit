package com.onair.hearit.application;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitSearchResponse;
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

    private final HearitRepository hearitRepository;

    public List<HearitSearchResponse> search(String searchTerm, PagingRequest pagingRequest) {
        String likeSearchTerm = "%" + searchTerm + "%";
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(likeSearchTerm, pageable);
        return hearits.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }
}
