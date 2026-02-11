package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HearitElasticSearchRepository extends ElasticsearchRepository<HearitDocument, Long>,
        HearitSearchRepository {

}
