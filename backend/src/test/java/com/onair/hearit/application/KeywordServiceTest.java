package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.KeywordRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(DbHelper.class)
@ActiveProfiles("fake-test")
class KeywordServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private KeywordRepository keywordRepository;

    private KeywordService keywordService;

    @BeforeEach
    void setup() {
        keywordService = new KeywordService(keywordRepository);
    }

    @Test
    @DisplayName("키워드 목록 조회 시 페이지네이션이 적용되어 반환된다.")
    void getKeywords_withPagination() {
        // given
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("keyword1"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("keyword2"));
        Keyword keyword3 = dbHelper.insertKeyword(new Keyword("keyword3"));
        Keyword keyword4 = dbHelper.insertKeyword(new Keyword("keyword4"));
        Keyword keyword5 = dbHelper.insertKeyword(new Keyword("keyword5"));

        PagingRequest pagingRequest = new PagingRequest(1, 2);// page = 1 (두 번째 페이지), size = 2

        // when
        List<KeywordResponse> result = keywordService.getKeywords(pagingRequest);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(KeywordResponse::id)
                        .containsExactly(keyword3.getId(), keyword4.getId())
        );
    }

    @Test
    @DisplayName("단일 키워드를 조회할 수 있다.")
    void getKeywordByIdById() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("keyword1"));

        // when
        KeywordResponse result = keywordService.getKeyword(keyword.getId());

        // then
        assertThat(result.id()).isEqualTo(keyword.getId());
        assertThat(result.name()).isEqualTo(keyword.getName());
    }

    @Test
    @DisplayName("존재하지 않는 키워드를 조회하면 NotFoundException이 발생한다.")
    void getKeyword_ById_notFound() {
        // given
        Long notExistId = 999L;

        // when & then
        assertThatThrownBy(() -> keywordService.getKeyword(notExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("keywordId");
    }

    @Test
    @DisplayName("오늘의 추천 키워드를 지정한 개수만큼 조회할 수 있다.")
    void getRecommendedKeywords() {
        // given
        dbHelper.insertKeyword(new Keyword("keyword1"));
        dbHelper.insertKeyword(new Keyword("keyword2"));
        dbHelper.insertKeyword(new Keyword("keyword3"));
        dbHelper.insertKeyword(new Keyword("keyword4"));
        dbHelper.insertKeyword(new Keyword("keyword5"));

        int size = 3;

        // when
        List<KeywordResponse> result = keywordService.getRecommendedKeywords(size);

        // then
        assertThat(result).hasSize(size);
    }

    @Test
    @DisplayName("같은 날 호출하면 추천 키워드 결과는 항상 동일하다.")
    void getRecommendedKeywords_shouldBeDeterministicForSameSeed() {
        // given
        dbHelper.insertKeyword(new Keyword("keyword1"));
        dbHelper.insertKeyword(new Keyword("keyword2"));
        dbHelper.insertKeyword(new Keyword("keyword3"));
        dbHelper.insertKeyword(new Keyword("keyword4"));
        dbHelper.insertKeyword(new Keyword("keyword5"));
        dbHelper.insertKeyword(new Keyword("keyword6"));
        int size = 3;

        // when
        List<KeywordResponse> first = keywordService.getRecommendedKeywords(size);
        List<KeywordResponse> second = keywordService.getRecommendedKeywords(size);

        // then
        assertThat(first).extracting(KeywordResponse::id)
                .containsExactlyElementsOf(
                        second.stream().map(KeywordResponse::id).toList()
                );
    }
} 
