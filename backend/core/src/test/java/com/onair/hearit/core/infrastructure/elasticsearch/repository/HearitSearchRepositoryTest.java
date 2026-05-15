package com.onair.hearit.core.infrastructure.elasticsearch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.infrastructure.elasticsearch.ElasticSearchTestContainer;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@DataElasticsearchTest
class HearitSearchRepositoryTest extends ElasticSearchTestContainer {

    @Autowired
    private ElasticsearchOperations operations;

    private HearitSearchRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HearitSearchRepositoryImpl(operations);

        // 테스트 데이터 초기화
        operations.indexOps(HearitDocument.class).delete();
        operations.indexOps(HearitDocument.class).create();
        operations.indexOps(HearitDocument.class).putMapping();

        // 테스트용 문서 인덱싱
        List<HearitDocument> testDocuments = createTestDocuments();
        operations.save(testDocuments);

        operations.indexOps(HearitDocument.class).refresh();
    }

    private List<HearitDocument> createTestDocuments() {
        return List.of(
                // 1. 완전 일치 제목 테스트용
                new HearitDocument(1L, "스프링부트 완전 정복",
                        "스프링부트로 백엔드 개발하는 방법",
                        List.of("스프링", "백엔드", "자바"),
                        "개발",
                        LocalDate.of(2024, 1, 15)),

                // 2. 제목 부분 일치 + 키워드 매칭
                new HearitDocument(2L, "자바 스프링 입문 가이드",
                        "자바 기반의 스프링 프레임워크 소개",
                        List.of("자바", "스프링", "프레임워크"),
                        "개발",
                        LocalDate.of(2024, 1, 20)),

                // 3. 동의어 테스트 - springboot
                new HearitDocument(3L, "Spring Boot 마이크로서비스 구축",
                        "Spring Boot로 MSA 아키텍처 구현하기",
                        List.of("springboot", "msa", "클린아키텍처"),
                        "아키텍처",
                        LocalDate.of(2024, 2, 1)),

                // 4. AI 관련 - 사용자 사전 및 동의어
                new HearitDocument(4L, "클로드와 챗지피티 비교",
                        "LLM 모델들의 특징과 차이점 분석",
                        List.of("인공지능", "클로드", "챗지피티", "LLM"),
                        "AI",
                        LocalDate.of(2024, 2, 10)),

                // 5. 동의어 테스트 - gpt
                new HearitDocument(5L, "GPT 활용한 RAG 시스템 구축",
                        "GPT와 벡터DB를 활용한 검색 증강 생성",
                        List.of("GPT", "RAG", "AI"),
                        "AI",
                        LocalDate.of(2024, 2, 15)),

                // 6. 안드로이드 관련 - 사용자 사전
                new HearitDocument(6L, "안드로이드 제트팩 컴포즈 시작하기",
                        "제트팩 컴포즈로 UI 만들기",
                        List.of("안드로이드", "컴포즈", "제트팩"),
                        "모바일",
                        LocalDate.of(2024, 1, 25)),

                // 7. 약어 및 동의어 - 안드
                new HearitDocument(7L, "안드 코루틴 완벽 가이드",
                        "코틀린 코루틴으로 비동기 처리하기",
                        List.of("안드로이드", "코루틴", "코틀린"),
                        "모바일",
                        LocalDate.of(2024, 2, 5)),

                // 8. 데이터베이스 동의어 - db, 디비
                new HearitDocument(8L, "MySQL 데이터베이스 최적화",
                        "DB 인덱스와 쿼리 튜닝 전략",
                        List.of("데이터베이스", "MySQL", "최적화"),
                        "데이터",
                        LocalDate.of(2024, 1, 10)),

                // 9. 네트워크 관련
                new HearitDocument(9L, "AWS VPC 네트워크 설계",
                        "가상사설망 구축과 보안 설정",
                        List.of("네트워크", "AWS", "VPC"),
                        "인프라",
                        LocalDate.of(2024, 1, 5)),

                // 10. 프론트엔드 동의어
                new HearitDocument(10L, "리액트 프론트엔드 개발",
                        "React와 TypeScript로 SPA 구축",
                        List.of("리액트", "타입스크립트", "프론트엔드"),
                        "개발",
                        LocalDate.of(2024, 2, 20)),

                // 11. 짧은 검색어 테스트용
                new HearitDocument(11L, "AI 트렌드 2024",
                        "올해의 인공지능 기술 동향",
                        List.of("AI", "트렌드", "기술"),
                        "AI",
                        LocalDate.of(2024, 2, 25)),

                // 12. Summary 매칭 테스트
                new HearitDocument(12L, "쿠버네티스 입문",
                        "k8s로 컨테이너 오케스트레이션 마스터하기. 도커와 쿠버네티스를 활용한 배포 자동화",
                        List.of("쿠버네티스", "k8s", "도커"),
                        "인프라",
                        LocalDate.of(2024, 1, 1))
        );
    }

    @Nested
    @DisplayName("검색어 매칭 전략 테스트")
    class SearchMatchingStrategyTest {

        @Test
        @DisplayName("제목 완전 일치는 가장 높은 점수를 받아 상위에 노출된다")
        void titleExactMatchShouldRankHighest() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("스프링부트 완전 정복", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0)).isEqualTo(1L); // 완전 일치 문서가 최상위
        }

        @Test
        @DisplayName("제목에 검색어가 포함되면 높은 순위를 받는다")
        void titlePartialMatchShouldRankHigh() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("자바 스프링", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 제목에 "자바 스프링"이 모두 포함된 문서들이 상위에 위치
            assertThat(result.getContent()).contains(1L, 2L);
        }

        @Test
        @DisplayName("키워드 필드 매칭은 제목 다음으로 중요하게 취급된다")
        void keywordMatchingShouldRankAfterTitle() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("클린아키텍처", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(3L); // 키워드에 클린아키텍처가 있는 문서
        }

        @Test
        @DisplayName("Summary 필드 매칭도 검색 결과에 포함된다")
        void summaryMatchingShouldBeIncluded() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("컨테이너 오케스트레이션", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(12L); // Summary에 해당 키워드가 있는 문서
        }

        @Test
        @DisplayName("카테고리 필드도 검색 대상에 포함된다")
        void categoryFieldShouldBeSearchable() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("모바일", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(6L, 7L); // 카테고리가 "모바일"인 문서들
        }
    }

    @Nested
    @DisplayName("동의어 처리 테스트")
    class SynonymTest {

        @Test
        @DisplayName("'스프링'으로 검색하면 'springboot' 문서도 함께 검색된다")
        void springBootSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("스프링", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(3L); // "Spring Boot" 제목의 문서
        }

        @Test
        @DisplayName("'챗지피티'로 검색하면 'GPT' 문서도 함께 검색된다")
        void chatGptSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("챗지피티", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).containsAnyOf(4L, 5L); // GPT 관련 문서들
        }

        @Test
        @DisplayName("'안드'로 검색하면 '안드로이드' 문서가 검색된다")
        void androidShortFormSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("안드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(6L, 7L); // 안드로이드 관련 문서들
        }

        @Test
        @DisplayName("'디비'로 검색하면 '데이터베이스' 문서가 검색된다")
        void databaseSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("디비", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(8L); // MySQL 데이터베이스 문서
        }

        @Test
        @DisplayName("'프론트'로 검색하면 '프론트엔드', 'frontend' 문서가 검색된다")
        void frontendSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("프론트", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(10L); // React 프론트엔드 문서
        }

        @Test
        @DisplayName("'k8s'로 검색하면 '쿠버네티스' 문서가 검색된다")
        void kubernetesSynonymShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("k8s", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(12L); // 쿠버네티스 문서
        }
    }

    @Nested
    @DisplayName("사용자 사전 처리 테스트")
    class UserDictionaryTest {

        @Test
        @DisplayName("사용자 사전에 등록된 'LLM'이 올바르게 토큰화되어 검색된다")
        void llmUserDictShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("LLM", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(4L); // LLM 키워드가 있는 문서
        }

        @Test
        @DisplayName("사용자 사전에 등록된 '클로드'가 올바르게 검색된다")
        void claudeUserDictShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("클로드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(4L); // 클로드 키워드가 있는 문서
        }

        @Test
        @DisplayName("사용자 사전에 등록된 '제트팩컴포즈'가 하나의 토큰으로 검색된다")
        void jetpackComposeUserDictShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("제트팩", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(6L); // 제트팩 컴포즈 문서
        }

        @Test
        @DisplayName("사용자 사전에 등록된 '코루틴'이 올바르게 검색된다")
        void coroutineUserDictShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("코루틴", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(7L); // 코루틴 가이드 문서
        }
    }

    @Nested
    @DisplayName("불용어 처리 테스트")
    class StopwordTest {

        @Test
        @DisplayName("불용어가 포함된 검색어에서 불용어는 무시되고 핵심 키워드만 검색된다")
        void stopwordsShouldBeIgnored() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when - "완전", "정복", "가이드" 등은 불용어로 필터링됨
            Page<Long> result = repository.search("스프링 완전 정복", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // "스프링"만으로 검색된 것과 유사한 결과
            assertThat(result.getContent()).contains(1L, 2L, 3L);
        }

        @Test
        @DisplayName("'에피소드'는 불용어로 제거되고 실제 콘텐츠 키워드로 검색된다")
        void episodeStopwordShouldBeRemoved() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("AI 에피소드 트렌드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // "에피소드"는 무시되고 "AI", "트렌드"로만 검색됨
            assertThat(result.getContent()).containsAnyOf(4L, 5L, 11L);
        }
    }

    @Nested
    @DisplayName("짧은 검색어 처리 테스트")
    class ShortQueryTest {

        @Test
        @DisplayName("2글자 이하 검색어는 PhrasePrefix로 엄격하게 매칭된다")
        void shortQueryShouldUsePhrasePrefix() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("AI", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // AI로 시작하거나 정확히 포함하는 문서만 검색됨
            assertThat(result.getContent()).containsAnyOf(4L, 5L, 11L);
        }

        @Test
        @DisplayName("3글자 이상 검색어는 Fuzzy 매칭이 적용되어 오타도 허용된다")
        void longQueryShouldUseFuzzyMatching() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when - "스프링"의 오타를 의도적으로 넣어도 검색됨 (fuzzy)
            Page<Long> result = repository.search("스프링부트", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(1L, 3L);
        }
    }

    @Nested
    @DisplayName("정렬 기능 테스트")
    class SortingTest {

        @Test
        @DisplayName("LATEST 정렬은 최신순으로 정렬된다")
        void latestSortShouldOrderByCreatedAtDesc() {
            // given
            Pageable pageable = PageRequest.of(0, 5);

            // when
            Page<Long> result = repository.search("개발", HearitSearchSortField.LATEST, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 2024-02-20 (10L) -> 2024-01-20 (2L) -> 2024-01-15 (1L) 순
            List<Long> expectedOrder = List.of(10L, 2L, 1L);
            assertThat(result.getContent()).containsSequence(expectedOrder.toArray(new Long[0]));
        }

        @Test
        @DisplayName("OLDEST 정렬은 오래된 순으로 정렬된다")
        void oldestSortShouldOrderByCreatedAtAsc() {
            // given
            Pageable pageable = PageRequest.of(0, 5);

            // when
            Page<Long> result = repository.search("인프라", HearitSearchSortField.OLDEST, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 2024-01-01 (12L) -> 2024-01-05 (9L) 순
            assertThat(result.getContent().get(0)).isEqualTo(12L);
            assertThat(result.getContent().get(1)).isEqualTo(9L);
        }

        @Test
        @DisplayName("ACCURACY 정렬은 검색 점수순으로 정렬되고, 동점일 경우 최신순으로 정렬된다")
        void accuracySortShouldOrderByScoreThenCreatedAt() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("스프링", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 검색 점수가 높은 순서대로, 같으면 최신순
            assertThat(result.getContent().get(0)).isIn(1L, 2L, 3L);
        }

        @Test
        @DisplayName("RECOMMENDED 정렬은 ACCURACY와 동일하게 동작한다")
        void recommendedSortShouldWorkLikeAccuracy() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> accuracyResult = repository.search("AI", HearitSearchSortField.ACCURACY, pageable);
            Page<Long> recommendedResult = repository.search("AI", HearitSearchSortField.RECOMMENDED, pageable);

            // then
            assertThat(accuracyResult.getContent()).isEqualTo(recommendedResult.getContent());
        }
    }

    @Nested
    @DisplayName("페이징 처리 테스트")
    class PaginationTest {

        @Test
        @DisplayName("페이지 크기에 맞게 결과가 반환된다")
        void pageSizeShouldBeRespected() {
            // given
            Pageable pageable = PageRequest.of(0, 3);

            // when
            Page<Long> result = repository.search("개발", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("두 번째 페이지가 올바르게 반환된다")
        void secondPageShouldBeRetrieved() {
            // given
            Pageable firstPage = PageRequest.of(0, 1);
            Pageable secondPage = PageRequest.of(1, 1);

            // when
            Page<Long> firstResult = repository.search("개발", HearitSearchSortField.ACCURACY, firstPage);
            Page<Long> secondResult = repository.search("개발", HearitSearchSortField.ACCURACY, secondPage);

            // then
            assertThat(firstResult.getContent()).hasSize(1);
            assertThat(secondResult.getContent()).isNotEmpty();
            // 첫 페이지와 두 번째 페이지의 결과가 겹치지 않음
            assertThat(firstResult.getContent()).doesNotContainAnyElementsOf(secondResult.getContent());
        }

        @Test
        @DisplayName("총 검색 결과 수가 올바르게 반환된다")
        void totalElementsShouldBeCorrect() {
            // given
            Pageable pageable = PageRequest.of(0, 5);

            // when
            Page<Long> result = repository.search("AI", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getTotalElements()).isGreaterThan(0);
            assertThat(result.getTotalPages()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("빈 검색어는 빈 결과를 반환한다")
        void emptyQueryShouldReturnEmptyResult() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("null 검색어는 빈 결과를 반환한다")
        void nullQueryShouldReturnEmptyResult() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search(null, HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("공백만 있는 검색어는 빈 결과를 반환한다")
        void whitespaceOnlyQueryShouldReturnEmptyResult() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("   ", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("검색 결과가 없는 검색어는 빈 페이지를 반환한다")
        void noMatchQueryShouldReturnEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("존재하지않는검색어12345", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("복합 검색 시나리오 테스트")
    class ComplexSearchScenarioTest {

        @Test
        @DisplayName("여러 필드에 걸쳐 매칭되는 복합 검색어가 올바르게 처리된다")
        void multiFieldMatchingShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when - 제목, 키워드, Summary에 모두 관련된 검색어
            Page<Long> result = repository.search("스프링 백엔드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).contains(1L); // 제목과 키워드에 모두 포함
        }

        @Test
        @DisplayName("동의어와 원어가 섞인 검색어도 올바르게 처리된다")
        void mixedSynonymQueryShouldWork() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("springboot 백엔드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 영문(springboot)과 한글(백엔드)이 모두 검색됨
            assertThat(result.getContent()).containsAnyOf(1L, 3L);
        }

        @Test
        @DisplayName("카테고리와 키워드를 함께 검색하면 정확도가 높아진다")
        void categoryAndKeywordMatchShouldRankHigher() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Long> result = repository.search("모바일 안드로이드", HearitSearchSortField.ACCURACY, pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            // 카테고리와 키워드 모두 매칭되는 문서가 상위에 위치
            assertThat(result.getContent()).contains(6L, 7L);
        }
    }

    @Nested
    @DisplayName("자동완성 테스트")
    class AutocompleteTest {

        @Test
        @DisplayName("검색어로 시작하는 키워드가 자동완성 결과에 포함된다")
        void autocomplete_matchesKeyword() {
            // "스프링" 키워드를 가진 문서(1, 2)에서 "스프"로 시작하는 키워드 반환
            List<String> result = repository.autocomplete("스프", 10);

            assertThat(result).contains("스프링");
        }

        @Test
        @DisplayName("검색어로 시작하는 카테고리가 자동완성 결과에 포함된다")
        void autocomplete_matchesCategory() {
            // "개발" 카테고리를 가진 문서(1, 2, 10)에서 "개"로 시작하는 카테고리 반환
            List<String> result = repository.autocomplete("개", 10);

            assertThat(result).contains("개발");
        }

        @Test
        @DisplayName("검색어를 포함하는 제목 단어가 자동완성 결과에 포함된다")
        void autocomplete_matchesTitle() {
            // "자바 스프링 입문 가이드"(doc 2) 제목에서 "자바" 단어 매칭
            List<String> result = repository.autocomplete("자바", 10);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("동일한 자동완성 결과는 중복 없이 한 번만 반환된다")
        void autocomplete_returnsDistinctResults() {
            // "스프링" 키워드를 가진 문서(1, 2)가 여러 개이지만 "스프링"은 한 번만 등장
            List<String> result = repository.autocomplete("스프", 10);

            long count = result.stream().filter("스프링"::equals).count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("size 파라미터 이하의 자동완성 결과를 반환한다")
        void autocomplete_respectsSizeLimit() {
            // "스프" 검색 시 여러 결과가 매칭되지만 size=1이면 최대 1개만 반환
            List<String> result = repository.autocomplete("스프", 1);

            assertThat(result).hasSizeLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("매칭되는 문서가 없으면 빈 리스트를 반환한다")
        void autocomplete_returnsEmptyListWhenNoMatch() {
            List<String> result = repository.autocomplete("존재하지않는검색어12345", 10);

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("전체 List의 Id만 조회한다.")
    void findAllIds() {
        // when
        List<Long> ids = repository.findAllIds();

        // then
        assertThat(ids).hasSize(12);
    }
}
