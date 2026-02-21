package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;

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

        // 정렬 적용 및 실행
        NativeQuery nativeQuery = applySort(nativeQueryBuilder, sortField);
        SearchHits<HearitDocument> searchResults = operations.search(nativeQuery, HearitDocument.class);
        List<Long> ids = searchResults.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();
        return new PageImpl<>(ids, pageable, searchResults.getTotalHits());
    }

    private NativeQuery applySort(NativeQueryBuilder builder, HearitSearchSortField sortField) {
        switch (sortField) {
            case LATEST -> builder.withSort(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc)));
            case OLDEST -> builder.withSort(s -> s.field(f -> f.field("created_at").order(SortOrder.Asc)));
            case ACCURACY, RECOMMENDED -> {
                // TODO: Recommend 조회수, 좋아요 등 추가 반영 필요
                builder.withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
                builder.withSort(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc)));
            }
        }
        return builder.build();
    }
}
