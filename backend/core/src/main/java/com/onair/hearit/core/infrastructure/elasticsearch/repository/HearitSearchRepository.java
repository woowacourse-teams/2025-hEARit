package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HearitSearchRepository {
    Page<Long> search(String query, HearitSearchSortField sortField, Pageable pageable);
}
