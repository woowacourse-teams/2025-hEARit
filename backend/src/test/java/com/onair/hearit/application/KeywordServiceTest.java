package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.KeywordResponse;
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
    @DisplayName("전체 키워드를 조회할 수 있다.")
    void getAllKeywords() {
        // given
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");
        Keyword keyword3 = saveKeyword("keyword3");

        // when
        List<KeywordResponse> result = keywordService.getKeywords();

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(KeywordResponse::id)
                        .contains(keyword1.getId(), keyword2.getId(), keyword3.getId())
        );
    }

    @Test
    @DisplayName("단일 키워드를 조회할 수 있다.")
    void getKeywordById() {
        // given
        Keyword keyword = saveKeyword("keyword");

        // when
        KeywordResponse result = keywordService.getKeyword(keyword.getId());

        // then
        assertThat(result.id()).isEqualTo(keyword.getId());
        assertThat(result.name()).isEqualTo(keyword.getName());
    }

    @Test
    @DisplayName("존재하지 않는 키워드를 조회하면 NotFoundException이 발생한다.")
    void getKeyword_notFound() {
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
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");
        Keyword keyword3 = saveKeyword("keyword3");
        Keyword keyword4 = saveKeyword("keyword4");
        Keyword keyword5 = saveKeyword("keyword5");

        int size = 3;

        // when
        List<KeywordResponse> result = keywordService.getRecommendedKeyword(1, size);

        // then
        assertThat(result).hasSize(size);
        assertThat(result).extracting(KeywordResponse::id)
                .allMatch(id -> List.of(
                        keyword1.getId(), keyword2.getId(), keyword3.getId(),
                        keyword4.getId(), keyword5.getId()
                ).contains(id));
    }

    @Test
    @DisplayName("같은 시드값으로 호출하면 추천 키워드 결과는 항상 동일하다.")
    void getRecommendedKeywords_shouldBeDeterministicForSameSeed() {
        // given
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");
        Keyword keyword3 = saveKeyword("keyword3");
        Keyword keyword4 = saveKeyword("keyword4");
        Keyword keyword5 = saveKeyword("keyword5");

        int size = 3;
        long fixedSeed = 20250722L;  // 시드 고정

        // when
        List<KeywordResponse> firstResponse = keywordService.getRecommendedKeyword(fixedSeed, size);
        List<KeywordResponse> secondResponse = keywordService.getRecommendedKeyword(fixedSeed, size);

        // then
        assertThat(firstResponse).hasSize(size);
        assertThat(secondResponse).hasSize(size);

        assertThat(firstResponse)
                .extracting(KeywordResponse::id)
                .containsExactlyElementsOf(
                        secondResponse.stream()
                                .map(KeywordResponse::id)
                                .toList()
                );
    }


    private Keyword saveKeyword(String name) {
        Keyword keyword = new Keyword(name);
        return dbHelper.insertKeyword(keyword);
    }
} 