package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HearitSearchRepositoryImpl implements HearitSearchRepository {

    private final ElasticsearchOperations operations;

    @Override
    public Page<Long> search(String query, HearitSearchSortField sortField, Pageable pageable) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) {
            return Page.empty();
        }

        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withQuery(b -> b.bool(bool -> {

                    // 가중치 기반 매칭 (정렬용)
                    // 제목 완전 일치: 15
                    bool.should(s -> s.match(m -> m
                            .field("title.keyword")
                            .query(q)
                            .boost(15.0f)
                    ));

                    // 제목 포함: 10
                    bool.should(s -> s.multiMatch(mm -> mm
                            .query(q)
                            .fields("title^10")
                            .type(TextQueryType.PhrasePrefix)
                    ));

                    // 카테고리나 키워드 포함: 5
                    bool.should(s -> s.multiMatch(mm -> mm
                            .query(q)
                            .fields("keywords.text^5", "category^5")
                            .type(TextQueryType.BestFields)
                    ));

                    // 요약 포함: 2.5
                    bool.should(s -> s.multiMatch(mm -> mm
                            .query(q)
                            .fields("summary^2.5")
                            .type(TextQueryType.BestFields)
                            .operator(Operator.And)
                    ));

                    // 검색어 길이에 따른 필수 필터링
                    // 짧은 검색어: 전방 일치(Prefix) 검색으로 "AI" -> "AI 기술" 등 매칭
                    if (q.length() <= 2) {
                        bool.must(m -> m.multiMatch(mm -> mm
                                .query(q)
                                .fields("title", "summary", "keywords.text", "category.text")
                                .type(TextQueryType.PhrasePrefix)
                                .operator(Operator.And)
                        ));
                        return bool;
                    }

                    // 긴 검색어: 오타 허용(Fuzzy) 및 75% 이상 일치 검색
                    bool.must(m -> m.multiMatch(mm -> mm
                            .query(q)
                            .fields("title", "summary", "keywords.text", "category.text")
                            .type(TextQueryType.BestFields)
                            .fuzziness("AUTO")
                            .minimumShouldMatch("75%")
                    ));
                    return bool;
                }))
                .withPageable(pageable);

        sortField.applySort(nativeQueryBuilder);
        NativeQuery nativeQuery = nativeQueryBuilder.build();
        SearchHits<HearitDocument> searchResults = operations.search(nativeQuery, HearitDocument.class);
        List<Long> ids = searchResults.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();
        return new PageImpl<>(ids, pageable, searchResults.getTotalHits());
    }

    public List<Long> findAllIds() {
        NativeQuery query = NativeQuery.builder()
                .withSourceFilter(new FetchSourceFilter(true, new String[]{"id"}, null))
                .withPageable(PageRequest.of(0, 10000))
                .build();

        return operations.search(query, HearitDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .map(HearitDocument::getId)
                .toList();
    }

    @Override
    public List<String> autocomplete(String searchTerm, int size) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return operations.search(buildAutocompleteQuery(searchTerm, size), HearitDocument.class)
                .getSearchHits()
                .stream()
                .flatMap(hit -> extractMatches(hit.getContent(), lowerSearchTerm))
                .distinct()
                .limit(size)
                .toList();
    }

    private Query buildAutocompleteQuery(String searchTerm, int size) {
        return NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(searchTerm)
                                .type(TextQueryType.BoolPrefix)
                                .fields(
                                        "title.autocomplete",
                                        "title.autocomplete._2gram",
                                        "title.autocomplete._3gram",
                                        "keywords.autocomplete",
                                        "keywords.autocomplete._2gram",
                                        "keywords.autocomplete._3gram",
                                        "category.autocomplete",
                                        "category.autocomplete._2gram",
                                        "category.autocomplete._3gram"
                                )
                        )
                )
                .withSourceFilter(new FetchSourceFilter(true, new String[]{"title", "keywords", "category"}, new String[]{}))
                .withMaxResults(size)
                .build();
    }

    private Stream<String> extractMatches(HearitDocument doc, String lowerSearchTerm) {
        return Stream.of(
                matchCategory(doc, lowerSearchTerm),
                matchKeyword(doc, lowerSearchTerm),
                matchTitle(doc, lowerSearchTerm)
        ).filter(Objects::nonNull);
    }

    private String matchCategory(HearitDocument doc, String lowerSearchTerm) {
        return doc.getCategory().toLowerCase().startsWith(lowerSearchTerm) ? doc.getCategory() : null;
    }

    private String matchTitle(HearitDocument doc, String lowerSearchTerm) {
        return doc.getTitle().toLowerCase().contains(lowerSearchTerm) ? doc.getTitle() : null;
    }

    private String matchKeyword(HearitDocument doc, String lowerSearchTerm) {
        return doc.getKeywords().stream()
                .filter(k -> k.toLowerCase().startsWith(lowerSearchTerm))
                .findFirst()
                .orElse(null);
    }
}
