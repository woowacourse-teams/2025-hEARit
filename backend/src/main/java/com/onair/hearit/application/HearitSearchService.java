package com.onair.hearit.application;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.CategorySearchCondition;
import com.onair.hearit.dto.request.KeywordSearchCondition;
import com.onair.hearit.dto.request.TitleSearchCondition;
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

    public List<HearitSearchResponse> searchByTitle(TitleSearchCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> result = hearitRepository.findByTitleOrderByCreatedAtDesc(condition.searchTerm(), pageable);
        return result.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }

    public List<HearitSearchResponse> searchByCategory(CategorySearchCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> result = hearitRepository.findByCategoryIdOrderByCreatedAtDesc(condition.categoryId(), pageable);
        return result.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }

    public List<HearitSearchResponse> searchByKeyword(KeywordSearchCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> result = hearitRepository.findByKeywordIdOrderByCreatedAtDesc(condition.keywordId(), pageable);
        return result.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }
}
