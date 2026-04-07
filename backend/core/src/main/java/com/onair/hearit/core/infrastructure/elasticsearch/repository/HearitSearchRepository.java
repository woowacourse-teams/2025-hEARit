package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HearitSearchRepository {

    Page<Long> search(String query, HearitSearchSortField sortField, Pageable pageable);

    List<Long> findAllIds();

    List<String> autocomplete(String searchTerm, int size);
}
