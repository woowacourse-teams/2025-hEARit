package com.onair.hearit.app.search.application;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.search.dto.HearitSearchResponse;
import com.onair.hearit.app.search.dto.SearchAutocompleteResponse;
import com.onair.hearit.app.search.dto.SearchSortRequest;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.elasticsearch.repository.HearitElasticSearchRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearitSearchService {

    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final HearitElasticSearchRepository hearitElasticSearchRepository;

    @Transactional(readOnly = true)
    public PagedResponse<HearitSearchResponse> search(String searchTerm, PagingRequest pagingRequest,
                                                      UserInfo userInfo) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.searchByTerm(toBooleanModeQuery(searchTerm), pageable);
        UUID userUuid = userInfo.getUuid();
        return PagedResponse.from(toHearitSearchResponse(hearits, userUuid));
    }

    private String toBooleanModeQuery(String searchTerm) {
        return Arrays.stream(searchTerm.trim().split("\\s+"))
                .map(this::sanitizeToken)
                .filter(token -> token.length() >= 2)
                .map(token -> "+" + token + "*")
                .collect(Collectors.joining(" "));
    }

    private String sanitizeToken(String token) {
        return token.replaceAll("[+\\-~<>()\"*@]", "");
    }

    private Page<HearitSearchResponse> toHearitSearchResponse(Page<Hearit> hearits, UUID userUuid) {
        List<Long> hearitIds = hearits.getContent().stream().map(Hearit::getId).toList();
        Map<Long, List<Keyword>> hearitKeywords = getHearitKeywords(hearitIds);
        Map<Long, PlayingHistory> playingHistories =
                playingHistoryRepository.findByUserUuidAndHearitIdIn(userUuid, hearitIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PlayingHistory::getHearitId,
                                playingHistory -> playingHistory));
        return hearits.map(hearit -> HearitSearchResponse.of(
                hearit,
                hearitKeywords.getOrDefault(hearit.getId(), Collections.emptyList()),
                playingHistories.get(hearit.getId())));
    }

    private Map<Long, List<Keyword>> getHearitKeywords(List<Long> hearitIds) {
        return hearitKeywordRepository.findByHearitIdIn(hearitIds)
                .stream()
                .collect(Collectors.groupingBy(hk -> hk.getHearit().getId(),
                        Collectors.mapping(HearitKeyword::getKeyword, Collectors.toList())));
    }

    @Transactional(readOnly = true)
    public PagedResponse<HearitSearchResponse> searchV2(String searchTerm,
                                                        SearchSortRequest sortRequest,
                                                        PagingRequest pagingRequest,
                                                        UserInfo userInfo) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Long> searchedHearitIds = hearitElasticSearchRepository.search(searchTerm, sortRequest.field(), pageable);
        Page<Hearit> hearits = getSortedHearits(pageable, searchedHearitIds);
        UUID userUuid = userInfo.getUuid();
        log.info("[SEARCH] keyword: '{}', results: {}, page: {}",
                searchTerm, searchedHearitIds.getTotalElements(), pageable.getPageNumber());
        return PagedResponse.from(toHearitSearchResponse(hearits, userUuid));
    }

    private Page<Hearit> getSortedHearits(Pageable pageable, Page<Long> searchedHearitIds) {
        List<Long> ids = searchedHearitIds.getContent();
        List<Hearit> unorderedHearits = hearitRepository.findAllByIdIn(ids);
        Map<Long, Hearit> hearitMap = unorderedHearits.stream()
                .collect(Collectors.toMap(Hearit::getId, Function.identity()));
        List<Hearit> sortedHearits = ids.stream()
                .map(hearitMap::get)
                .filter(Objects::nonNull)
                .toList();
        return new PageImpl<>(sortedHearits, pageable, searchedHearitIds.getTotalElements());
    }

    public SearchAutocompleteResponse getAutocomplete(String searchTerm, int size) {
        List<String> autocompletes = hearitElasticSearchRepository.autocomplete(searchTerm, size);
        return new SearchAutocompleteResponse(autocompletes);
    }
}
