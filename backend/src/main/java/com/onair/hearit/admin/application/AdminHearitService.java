package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.HearitCreateRequest;
import com.onair.hearit.admin.dto.request.HearitCreateRequest.SourceCreateRequest;
import com.onair.hearit.admin.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest.SourceUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminHearitResponse;
import com.onair.hearit.admin.dto.response.AdminHearitResponse.KeywordInHearit;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.FileType;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Source;
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
    private final FileStorage fileStorage;

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
    public void addHearit(HearitCreateRequest request) {
        Category category = getCategoryById(request.categoryId());
        List<Source> sources = parseSourceCreateRequestToSource(request.sources());
        String originalAudioUrl = FileType.ORIGINAL.getUploadPath() + request.originalAudio().getOriginalFilename();
        String shortAudioUrl = FileType.SHORT.getUploadPath() + request.shortAudio().getOriginalFilename();
        String scriptUrl = FileType.SCRIPT.getUploadPath() + request.scriptFile().getOriginalFilename();

        Hearit hearit = new Hearit(request.title(), request.summary(), request.playTime(), originalAudioUrl,
                shortAudioUrl, scriptUrl, sources, category);

        fileStorage.uploadFile(request.originalAudio(), FileType.ORIGINAL);
        fileStorage.uploadFile(request.shortAudio(), FileType.SHORT);
        fileStorage.uploadFile(request.scriptFile(), FileType.SCRIPT);

        Hearit savedHearit = hearitRepository.save(hearit);
        saveHearitKeywords(request.keywordIds(), savedHearit);
    }

    private List<Source> parseSourceCreateRequestToSource(List<SourceCreateRequest> sources) {
        return sources.stream()
                .map(s -> new Source(s.sourceName(), s.sourceUrl()))
                .toList();
    }

    private void saveHearitKeywords(List<Long> keywordIds, Hearit savedHearit) {
        if (existsKeywords(keywordIds)) {
            List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
            validateHearitKeywords(keywordIds, keywords);
            List<HearitKeyword> hearitKeywords = keywords.stream()
                    .map(keyword -> new HearitKeyword(savedHearit, keyword))
                    .toList();
            hearitKeywordRepository.saveAll(hearitKeywords);
        }
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
        Category category = getCategoryById(request.categoryId());
        List<Source> sources = parseSourceUpdateRequestToSource(request.sources());
        Hearit hearit = getHearitById(hearitId);
        hearit.updateMetaData(request.title(), request.summary(), request.playTime(), sources, category);
    }

    private List<Source> parseSourceUpdateRequestToSource(List<SourceUpdateRequest> sources) {
        return sources.stream()
                .map(s -> new Source(s.sourceName(), s.sourceUrl()))
                .toList();
    }

    @Transactional
    public void modifyHearitFile(Long hearitId, HearitFileUpdateRequest request, FileType fileType) {
        Hearit hearit = getHearitById(hearitId);
        fileStorage.deleteFile(hearit.getFileUrl(fileType));
        String uploadFilePath = fileStorage.uploadFile(request.file(), fileType);
        hearit.updateFileUrl(uploadFilePath, fileType);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AdminNotFoundException("categoryId", categoryId.toString()));
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new AdminNotFoundException("hearitId", hearitId.toString()));
    }
}
