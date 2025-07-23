package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import java.time.LocalDateTime;
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
@Import(DbHelper.class)
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
        saveHearitWithSuffix(1);
        saveHearitWithSuffix(2);

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
        saveHearitWithSuffix(1);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Hearit> hearits = hearitRepository.findRandom(pageable);

        // then
        assertThat(hearits.getTotalElements()).isEqualTo(hearitRepository.findAll().size());
    }

    @Test
    @DisplayName("제목 또는 키워드에 검색어가 포함된 히어릿만 반환한다.")
    void searchByTerm_filterByTitleOrKeyword() {
        // given
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("SpringKeyword"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("NotMatched"));

        Hearit titleMatched = saveHearitWithTitleAndKeyword("SpringBoot is great", keyword2); // 제목만 매칭
        Hearit keywordMatched = saveHearitWithTitleAndKeyword("No match in title", keyword1); // 키워드만 매칭
        Hearit notMatched = saveHearitWithTitleAndKeyword("No match at all", keyword2);       // 둘 다 매칭 안 됨

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("%spring%", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent()).extracting(Hearit::getTitle)
                        .containsExactlyInAnyOrder(
                                titleMatched.getTitle(),
                                keywordMatched.getTitle()
                        )
        );
    }

    @Test
    @DisplayName("제목과 키워드 둘 다 검색어가 포함돼도 중복 없이 하나만 반환된다.")
    void searchByTerm_avoidDuplicateWhenTitleAndKeywordMatch() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("springboot"));
        Hearit hearit = saveHearitWithTitleAndKeyword("SpringBoot", keyword); // 제목, 키워드 모두 매칭

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("%spring%", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getId()).isEqualTo(hearit.getId())
        );
    }


    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix, "#123");
        dbHelper.insertCategory(category);

        Hearit hearit = new Hearit(
                "title" + suffix,
                "summary" + suffix, suffix,
                "originalAudioUrl" + suffix,
                "shortAudioUrl" + suffix,
                "scriptUrl" + suffix,
                "source" + suffix,
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }

    private Hearit saveHearitByTitle(String title) {
        Category category = new Category("category", "#123");
        dbHelper.insertCategory(category);
        Hearit hearit = new Hearit(
                title,
                "summary",
                1,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }

    private Category saveCategory(String name, String colorCode) {
        Category category = new Category(name, colorCode);
        return dbHelper.insertCategory(category);
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = saveCategory("category", "#abc");
        Hearit hearit = new Hearit(title, "summary", 1, "originalAudioUrl", "shortAudioUrl", "scriptUrl", "source",
                LocalDateTime.now(), category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
