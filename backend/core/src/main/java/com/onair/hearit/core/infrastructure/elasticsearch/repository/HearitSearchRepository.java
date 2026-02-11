package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import java.util.List;

public interface HearitSearchRepository {
    List<Long> search(String query);
}
