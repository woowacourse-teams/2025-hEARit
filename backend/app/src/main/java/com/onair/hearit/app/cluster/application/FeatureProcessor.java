package com.onair.hearit.app.cluster.application;

import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.projection.HearitClusterStatisticsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class FeatureProcessor {

    private final HearitRepository hearitRepository;
    private final HearitClusterCommandRepository hearitClusterCommandRepository;

    @Transactional
    public boolean processPage(int pageNumber, int pageSize) {
        Page<HearitClusterStatisticsProjection> page = hearitRepository.findClusterStatistics(
                PageRequest.of(pageNumber, pageSize)
        );

        if (!page.isEmpty()) {
            hearitClusterCommandRepository.upsertStatistics(page.getContent());
        }
        return page.hasNext();
    }
}
