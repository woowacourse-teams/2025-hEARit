package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({DbHelper.class, TestJpaAuditingConfig.class})
public class HearitRepositorySearchTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Test
    @DisplayName("제목과 키워드 둘 다 검색어가 포함돼도 중복 없이 하나만 반환된다.")
    void searchByTerm_avoidDuplicateWhenTitleAndKeywordMatch() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("springboot"));
        Hearit hearit = saveHearitWithTitleAndKeyword("SpringBoot", keyword);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Page<Hearit> result = hearitRepository.searchByTerm("spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getId()).isEqualTo(hearit.getId())
        );
    }

    @Test
    @DisplayName("제목 또는 키워드에 검색어가 포함된 히어릿을 반환한다.")
    void searchByTerm_filterByTitleOrKeyword() {
        // given
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Spring"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("Java"));

        Hearit titleMatched = saveHearitWithTitleAndKeyword("Spring", keyword2); // 제목만 매칭
        Hearit keywordMatched = saveHearitWithTitleAndKeyword("Kotlin", keyword1); // 키워드만 매칭
        Hearit notMatched = saveHearitWithTitleAndKeyword("Kotlin", keyword2);       // 둘 다 매칭 안 됨

        Pageable pageable = PageRequest.of(0, 10);

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Page<Hearit> result = hearitRepository.searchByTerm("Spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent()).extracting(Hearit::getTitle)
                        .containsExactlyInAnyOrder(
                                titleMatched.getTitle(),
                                keywordMatched.getTitle())
        );
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("출처", "url")),
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
