package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class HearitRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Test
    @DisplayName("원하는 개수만큼 랜덤 히어릿을 조회할 수 있다.")
    void findRandom() {
        // given
        Category category = saveCategory();
        Hearit hearit1 = saveHearit(category);
        Hearit hearit2 = saveHearit(category);

        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<Hearit> hearits = hearitRepository.findRandom(pageable);

        // then
        assertAll(() -> {
            assertThat(hearits).hasSize(1);
            assertThat(hearitRepository.findAll()).hasSize(2);
        });
    }

    @Test
    @DisplayName("전체 히어릿 개수 < 원하는 개수면 전체 히어릿을 모두 조회할 수 있다.")
    void findRandomWithAllHearits() {
        // given
        Category category = saveCategory();
        Hearit hearit1 = saveHearit(category);
        Hearit hearit2 = saveHearit(category);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Hearit> hearits = hearitRepository.findRandom(pageable);

        // then
        assertThat(hearits.getTotalElements()).isEqualTo(hearitRepository.findAll().size());
    }

    @Test
    @DisplayName("제목 또는 키워드에 검색어가 포함된 히어릿을 반환한다.")
    void searchByTerm_filterByTitleOrKeyword() {
        // given
        Keyword keyword1 = saveKeyword("Springboot");
        Keyword keyword2 = saveKeyword("NotMatched");

        Hearit titleMatched = saveHearitWithTitleAndKeyword("SpringBoot is great", keyword2); // 제목만 매칭
        Hearit keywordMatched = saveHearitWithTitleAndKeyword("No match in title", keyword1); // 키워드만 매칭
        Hearit notMatched = saveHearitWithTitleAndKeyword("No match at all", keyword2);       // 둘 다 매칭 안 됨

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent()).extracting(Hearit::getTitle)
                        .containsExactlyInAnyOrder(
                                titleMatched.getTitle(),
                                keywordMatched.getTitle())
        );
    }

    @Test
    @DisplayName("제목과 키워드 둘 다 검색어가 포함돼도 중복 없이 하나만 반환된다.")
    void searchByTerm_avoidDuplicateWhenTitleAndKeywordMatch() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("springboot"));
        Hearit hearit = saveHearitWithTitleAndKeyword("SpringBoot", keyword);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getId()).isEqualTo(hearit.getId())
        );
    }

    private Category saveCategory() {
        return dbHelper.insertCategory(TestFixture.createFixedCategory());
    }

    private Hearit saveHearit(Category category) {
        return dbHelper.insertHearit(TestFixture.createFixedHearit(category));
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = saveCategory();
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }
}
