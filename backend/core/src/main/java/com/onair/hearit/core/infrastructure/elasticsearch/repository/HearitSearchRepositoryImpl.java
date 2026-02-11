package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;

@RequiredArgsConstructor
public class HearitSearchRepositoryImpl implements HearitSearchRepository {

    private final ElasticsearchOperations operations;

    @Override
    public List<Long> search(String query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .query(query)
                        .fields("title", "summary", "keyword.text", "category")
                        .fuzziness("AUTO")))
                .build();
        SearchHits<HearitDocument> searchResults = operations.search(nativeQuery, HearitDocument.class);
        return searchResults.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();
    }
}
