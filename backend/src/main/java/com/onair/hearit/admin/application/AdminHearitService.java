package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.HearitUploadRequest;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.CategoryInfoResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminHearitService {

    private final HearitRepository hearitRepository;
    private final CategoryRepository categoryRepository;

    public HearitDetailResponse uploadHearit(HearitUploadRequest request) {
        Category category = getCategoryById(request.categoryId());
        Hearit hearit = new Hearit(request.title(), request.summary(), request.playTime(), request.originalAudioUrl(),
                request.shortAudioUrl(), request.scriptUrl(), request.source(), category);
        Hearit saved = hearitRepository.save(hearit);
        return HearitDetailResponse.from(saved);
    }

    public List<CategoryInfoResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryInfoResponse::from)
                .toList();
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("categoryId", categoryId.toString()));
    }
}
