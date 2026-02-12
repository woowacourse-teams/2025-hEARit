package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest.SourceUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest.SourceCreateRequest;
import com.onair.hearit.admin.dto.response.AdminHearitResponse;
import com.onair.hearit.admin.dto.response.AdminHearitResponse.KeywordInHearit;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.FileType;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.infrastructure.elasticsearch.event.HearitCreatedEvent;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.KeywordRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminHearitService {

    private final HearitRepository hearitRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher eventPublisher;

    public AdminPagedResponse<AdminHearitResponse> getHearits(AdminPagingRequest pagingRequest) {
        Pageable pageable = getHearitOrderByIdDesc(pagingRequest);
        Page<Hearit> hearits = hearitRepository.findAll(pageable);
        List<Long> hearitIds = extractHearitIds(hearits);
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);
        Map<Long, List<KeywordInHearit>> keywordMap = mapKeywordsByHearitId(hearitKeywords);
        Page<AdminHearitResponse> hearitDtos = hearits.map(
                hearit -> AdminHearitResponse.from(hearit, keywordMap.getOrDefault(hearit.getId(), List.of())));
        return AdminPagedResponse.from(hearitDtos);
    }

    private Pageable getHearitOrderByIdDesc(AdminPagingRequest pagingRequest) {
        Sort sort = Sort.by(Sort.Order.desc("id"));
        return PageRequest.of(pagingRequest.page(), pagingRequest.size(), sort);
    }

    private List<Long> extractHearitIds(Page<Hearit> hearits) {
        return hearits.getContent().stream()
                .map(Hearit::getId)
                .toList();
    }

    private Map<Long, List<KeywordInHearit>> mapKeywordsByHearitId(List<HearitKeyword> hearitKeywords) {
        return hearitKeywords.stream().collect(
                Collectors.groupingBy(hk -> hk.getHearit().getId(),
                        Collectors.mapping(hk -> new KeywordInHearit(hk.getKeyword().getName()),
                                Collectors.toList())));
    }

    @Transactional
    public void addHearitMetaData(HearitMetaDataRequest request) {
        try {
            Category category = getCategoryById(request.categoryId());
            List<Source> sources = mapSourceCreateRequestToSource(request.sources());
            Hearit hearit = new Hearit(
                    request.title(),
                    request.summary(),
                    request.playTime(),
                    request.originalAudioKey(),
                    request.shortAudioKey(),
                    request.scriptFileKey(),
                    sources,
                    category);
            Hearit savedHearit = hearitRepository.save(hearit);
            List<Keyword> keywords = saveHearitKeywords(request.keywordIds(), savedHearit);
            eventPublisher.publishEvent(HearitCreatedEvent.of(savedHearit, category, keywords));
        } catch (RuntimeException e) {
            deleteFile(FileType.ORIGINAL, request.originalAudioKey());
            deleteFile(FileType.SHORT, request.shortAudioKey());
            deleteFile(FileType.SCRIPT, request.scriptFileKey());
            log.warn("히어릿 메타 데이터 저장 중 예외 발생, S3 파일을 삭제합니다. request: {}, cause: {}",
                    request, e.getMessage(), e);
            throw e;
        }
    }

    private void deleteFile(FileType type, String key) {
        try {
            fileStorage.deleteFile(key);
        } catch (Exception ex) {
            log.warn("S3 정리 중 추가 예외 발생. type: {}, key: {}, cause: {}", type, key, ex.getMessage(), ex);
        }
    }

    private List<Source> mapSourceCreateRequestToSource(List<SourceCreateRequest> sources) {
        return sources.stream()
                .map(s -> new Source(s.sourceName(), s.sourceUrl()))
                .toList();
    }

    private List<Keyword> saveHearitKeywords(List<Long> keywordIds, Hearit savedHearit) {
        if (existsKeywords(keywordIds)) {
            List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
            validateHearitKeywords(keywordIds, keywords);
            List<HearitKeyword> hearitKeywords = keywords.stream()
                    .map(keyword -> new HearitKeyword(savedHearit, keyword))
                    .toList();
            hearitKeywordRepository.saveAll(hearitKeywords);
            return keywords;
        }
        return List.of();
    }

    private boolean existsKeywords(List<Long> keywordIds) {
        return keywordIds != null && !keywordIds.isEmpty();
    }

    private void validateHearitKeywords(List<Long> keywordIds, List<Keyword> keywords) {
        if (keywords.size() != keywordIds.size()) {
            throw new AdminNotFoundException("keywordIds", keywordIds.toString());
        }
    }

    @Transactional
    public void modifyHearitMetaData(Long hearitId, HearitInfoUpdateRequest request) {
        //TODO: 키워드 수정 추가 필요
        Category category = getCategoryById(request.categoryId());
        List<Source> sources = mapSourceUpdateRequestToSource(request.sources());
        Hearit hearit = getHearitById(hearitId);
        hearit.updateMetaData(request.title(), request.summary(), request.playTime(), sources, category);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearitId);
        eventPublisher.publishEvent(HearitCreatedEvent.of(hearit, category, keywords));
    }

    private List<Source> mapSourceUpdateRequestToSource(List<SourceUpdateRequest> sources) {
        return sources.stream()
                .map(s -> new Source(s.sourceName(), s.sourceUrl()))
                .toList();
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AdminNotFoundException("categoryId", categoryId.toString()));
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findByIdWithCategoryAndSources(hearitId)
                .orElseThrow(() -> new AdminNotFoundException("hearitId", hearitId.toString()));
    }
}
