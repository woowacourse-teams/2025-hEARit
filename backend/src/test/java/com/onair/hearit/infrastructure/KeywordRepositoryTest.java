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
    @DisplayName("저장된 키워드들의 ID 목록을 조회할 수 있다.")
    void findAllIds_returnsAllKeywordIds() {
        // given
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");
        Keyword keyword3 = saveKeyword("keyword3");

        // when
        List<Long> ids = keywordRepository.findAllIds();

        // then
        assertThat(ids).containsExactlyInAnyOrder(keyword1.getId(), keyword2.getId(), keyword3.getId());
    }

    @Test
    @DisplayName("ID 목록으로 키워드를 조회할 수 있다.")
    void findAllByIdIn_returnsMatchingKeywords() {
        // given
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");
        saveKeyword("keyword3");

        List<Long> selectedIds = List.of(keyword1.getId(), keyword2.getId());

        // when
        List<Keyword> result = keywordRepository.findAllByIdIn(selectedIds);

        // then
        assertThat(result).extracting(Keyword::getId)
                .containsExactlyInAnyOrderElementsOf(selectedIds);
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }
}
