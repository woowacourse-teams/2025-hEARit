package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Keyword;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(DbHelper.class)
@ActiveProfiles("fake-test")
class KeywordRepositoryTest {

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("지정한 개수만큼 랜덤 키워드를 조회할 수 있다.")
    void findRandomWithSeed_returnsSpecifiedSize() {
        // given
        saveKeyword("keyword1");
        saveKeyword("keyword2");
        saveKeyword("keyword3");
        saveKeyword("keyword4");
        saveKeyword("keyword5");

        int size = 3;
        long seed = 12345L;

        // when
        List<Keyword> result = keywordRepository.findRandomWithSeed(seed, size);

        // then
        assertThat(result).hasSize(size);
    }

    @Test
    @DisplayName("전체 키워드 수보다 더 많은 개수를 요청하면 모든 키워드를 반환한다.")
    void findRandomWithSeed_whenSizeExceedsTotal_returnsAll() {
        // given
        saveKeyword("keyword1");
        saveKeyword("keyword2");

        int requestedSize = 5;
        long seed = 999L;

        // when
        List<Keyword> result = keywordRepository.findRandomWithSeed(seed, requestedSize);

        // then
        assertThat(result).hasSize(2);
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }
}
