package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.HearitUploadRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.admin.dto.response.HearitAdminResponse.KeywordInHearit;
import com.onair.hearit.admin.dto.response.PagedResponse;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PagedResponse<HearitAdminResponse> getPageHearits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt")));
        Page<Hearit> pageHearits = hearitRepository.findAllForAdmin(pageable);
        List<Long> hearitIds = pageHearits.getContent().stream()
                .map(Hearit::getId)
                .toList();
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);

        Map<Long, List<KeywordInHearit>> keywordMap = hearitKeywords.stream()
                .collect(Collectors.groupingBy(
                        hk -> hk.getHearit().getId(),
                        Collectors.mapping(
                                hk -> new KeywordInHearit(hk.getKeyword().getName()),
                                Collectors.toList()
                        )
                ));
        Page<HearitAdminResponse> dtoPage = pageHearits.map(h -> HearitAdminResponse.from(h, keywordMap));
        return PagedResponse.from(dtoPage);
    }

    @Transactional
    public HearitDetailResponse uploadHearit(HearitUploadRequest request) {
        Category category = getCategoryById(request.categoryId());
        Hearit hearit = new Hearit(request.title(), request.summary(), request.playTime(), request.originalAudioUrl(),
                request.shortAudioUrl(), request.scriptUrl(), request.source(), category);
        Hearit savedHearit = hearitRepository.save(hearit);
        saveHearitKeywords(request.keywordIds(), savedHearit);
        return HearitDetailResponse.from(savedHearit);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("categoryId", categoryId.toString()));
    }

    private void saveHearitKeywords(List<Long> keywordIds, Hearit savedHearit) {
        if (!existsKeywords(keywordIds)) {
            return;
        }
        List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
        validateHearitKeywords(keywordIds, keywords);
        List<HearitKeyword> hearitKeywords = keywords.stream()
                .map(keyword -> new HearitKeyword(savedHearit, keyword))
                .toList();
        hearitKeywordRepository.saveAll(hearitKeywords);
    }

    private boolean existsKeywords(List<Long> keywordIds) {
        return keywordIds != null && !keywordIds.isEmpty();
    }

    private void validateHearitKeywords(List<Long> keywordIds, List<Keyword> keywords) {
        if (keywords.size() != keywordIds.size()) {
            throw new NotFoundException("keywordIds", keywordIds.toString());
        }
    }

    public void deleteHearitById(Long hearitId) {
        hearitRepository.deleteById(hearitId);
    }
}
