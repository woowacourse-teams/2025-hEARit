package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.HearitUploadRequest;
import com.onair.hearit.admin.dto.KeywordInfoResponse;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.CategoryInfoResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHearitService {

    private final HearitRepository hearitRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    @Transactional
    public HearitDetailResponse uploadHearit(HearitUploadRequest request) {
        Category category = getCategoryById(request.categoryId());
        Hearit hearit = new Hearit(request.title(), request.summary(), request.playTime(), request.originalAudioUrl(),
                request.shortAudioUrl(), request.scriptUrl(), request.source(), category);
        Hearit savedHearit = hearitRepository.save(hearit);

        if (request.keywordIds() != null && !request.keywordIds().isEmpty()) {
            List<Keyword> keywords = keywordRepository.findAllById(request.keywordIds());
            if (keywords.size() != request.keywordIds().size()) {
                throw new NotFoundException("keywordIds", request.keywordIds().toString());
            }

            List<HearitKeyword> hearitKeywords = keywords.stream()
                    .map(keyword -> new HearitKeyword(savedHearit, keyword))
                    .toList();
            hearitKeywordRepository.saveAll(hearitKeywords);
        }

        return HearitDetailResponse.from(savedHearit);
    }

    public List<CategoryInfoResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryInfoResponse::from)
                .toList();
    }

    public List<KeywordInfoResponse> getAllKeywords() {
        List<Keyword> keywords = keywordRepository.findAll();
        return keywords.stream()
                .map(KeywordInfoResponse::from)
                .toList();
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("categoryId", categoryId.toString()));
    }
}
