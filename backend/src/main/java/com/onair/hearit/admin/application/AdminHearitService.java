package com.onair.hearit.admin.application;

import com.onair.hearit.admin.domain.FileType;
import com.onair.hearit.admin.domain.FileValidator;
import com.onair.hearit.admin.dto.request.HearitCreateRequest;
import com.onair.hearit.admin.dto.request.HearitUpdateRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.admin.dto.response.HearitAdminResponse.KeywordInHearit;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
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
public class AdminHearitService {

    private final HearitRepository hearitRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final S3Uploader s3Uploader;

    public PagedResponse<HearitAdminResponse> getHearits(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(
                pagingRequest.page(), pagingRequest.size(), Sort.by(Sort.Order.desc("id")));
        Page<Hearit> hearits = hearitRepository.findAll(pageable);
        List<Long> hearitIds = extractHearitIds(hearits);
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);
        Map<Long, List<KeywordInHearit>> keywordMap = mapKeywordsByHearitId(hearitKeywords);

        Page<HearitAdminResponse> hearitDtos = hearits.map(h -> HearitAdminResponse.from(h, keywordMap));
        return PagedResponse.from(hearitDtos);
    }

    private List<Long> extractHearitIds(Page<Hearit> hearits) {
        return hearits.getContent().stream()
                .map(Hearit::getId)
                .toList();
    }

    private Map<Long, List<KeywordInHearit>> mapKeywordsByHearitId(List<HearitKeyword> hearitKeywords) {
        return hearitKeywords.stream()
                .collect(Collectors.groupingBy(
                        hk -> hk.getHearit().getId(),
                        Collectors.mapping(
                                hk -> new KeywordInHearit(hk.getKeyword().getName()),
                                Collectors.toList()
                        )
                ));
    }

    @Transactional
    public void addHearit(HearitCreateRequest request) {
        FileValidator.validateAll(request.originalAudio(), request.shortAudio(), request.scriptFile());
        String originalAudioPath = s3Uploader.uploadFile(request.originalAudio(), FileType.ORIGINAL);
        String shortAudioPath = s3Uploader.uploadFile(request.shortAudio(), FileType.SHORT);
        String scriptFilePath = s3Uploader.uploadFile(request.scriptFile(), FileType.SCRIPT);

        Category category = getCategoryById(request.categoryId());
        Hearit hearit = new Hearit(request.title(), request.summary(), request.playTime(), originalAudioPath,
                shortAudioPath, scriptFilePath, request.source(), category);
        Hearit savedHearit = hearitRepository.save(hearit);
        saveHearitKeywords(request.keywordIds(), savedHearit);
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

    @Transactional
    public void modifyHearit(Long hearitId, HearitUpdateRequest request) {
        Category category = getCategoryById(request.categoryId());
        Hearit hearit = hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));

        hearit.update(request.title(), request.summary(), request.playTime(), request.originalAudioUrl(),
                request.shortAudioUrl(), request.scriptUrl(), request.source(), category);
    }
}
