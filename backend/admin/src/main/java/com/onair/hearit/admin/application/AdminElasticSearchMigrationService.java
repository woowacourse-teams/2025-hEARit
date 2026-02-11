package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.response.AdminElasticSearchMigrationResponse;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import com.onair.hearit.core.infrastructure.elasticsearch.repository.HearitElasticSearchRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
        Set<Long> existElasticIds = StreamSupport
                .stream(hearitElasticSearchRepository.findAll().spliterator(), false)
                .map(HearitDocument::getId)
                .collect(Collectors.toSet());

        return hearitRepository.findAll().stream()
                .map(Hearit::getId)
                .filter(id -> !existElasticIds.contains(id))
                .toList();
    }

    private List<HearitDocument> getHearitDocuments(List<Long> notMigrationIds) {
        return hearitRepository.findAllByIdIn(notMigrationIds).stream()
                .map(hearit -> {
                    List<String> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId()).stream()
                            .map(Keyword::getName).toList();
                    return new HearitDocument(hearit.getId(), hearit.getTitle(), hearit.getSummary(), keywords,
                            hearit.getCategory().getName(), hearit.getCreatedAt().toLocalDate());

                })
                .toList();
    }
}
