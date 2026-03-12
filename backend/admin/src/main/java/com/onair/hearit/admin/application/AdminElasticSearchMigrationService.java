package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.response.AdminElasticSearchMigrationResponse;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import com.onair.hearit.core.infrastructure.elasticsearch.repository.HearitElasticSearchRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminElasticSearchMigrationService {

    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final HearitElasticSearchRepository hearitElasticSearchRepository;

    @Transactional
    public AdminElasticSearchMigrationResponse migrate() {
        List<Long> missingMigrationIds = getMissingMigrationIds();
        if (missingMigrationIds.isEmpty()) {
            return new AdminElasticSearchMigrationResponse("마이그레이션이 이미 되어있습니다.", 0);
        }

        List<HearitDocument> hearitDocuments = getHearitDocuments(missingMigrationIds);
        hearitElasticSearchRepository.saveAll(hearitDocuments);
        return new AdminElasticSearchMigrationResponse(
                missingMigrationIds.size() + " 건 마이그레이션에 성공했습니다.",
                missingMigrationIds.size());
    }

    private List<Long> getMissingMigrationIds() {
        List<Long> existElasticIds = hearitElasticSearchRepository.findAllIds();
        return hearitRepository.findAllIds().stream()
                .filter(id -> !existElasticIds.contains(id))
                .toList();
    }

    private List<HearitDocument> getHearitDocuments(List<Long> notMigrationIds) {
        Map<Long, List<Keyword>> keywordsMap = getKeywordsMap(notMigrationIds);
        return hearitRepository.findAllByIdIn(notMigrationIds).stream()
                .map(hearit -> {
                    List<Keyword> keywords = keywordsMap.getOrDefault(hearit.getId(), Collections.emptyList());
                    return HearitDocument.of(hearit, hearit.getCategory(), keywords);
                })
                .toList();
    }

    private Map<Long, List<Keyword>> getKeywordsMap(List<Long> hearitIds) {
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);
        return hearitKeywords.stream()
                .collect(Collectors.groupingBy(
                        hk -> hk.getHearit().getId(),
                        Collectors.mapping(HearitKeyword::getKeyword, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .toList()
                ));
    }
}
