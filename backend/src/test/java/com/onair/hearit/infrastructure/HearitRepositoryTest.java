package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;
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
        List<Hearit> hearits = hearitRepository.findRandom(pageable);

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
        List<Hearit> hearits = hearitRepository.findRandom(pageable);

        // then
        assertThat(hearits.size()).isEqualTo(hearitRepository.findAll().size());
    }

    @Test
    @DisplayName("제목에 특정 키워드가 포함된 히어릿만 조회할 수 있다.")
    void findByTitleContainingIgnoreCase() {
        // given
        Hearit hearit1 = saveHearitByTitle("exampletitle1");
        Hearit hearit2 = saveHearitByTitle("title1example");
        Hearit hearit3 = saveHearitByTitle("wwtitle1ww");
        Hearit hearit4 = saveHearitByTitle("notitle");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.findByTitleOrderByCreatedAtDesc("title1", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent()).extracting(Hearit::getTitle)
                        .allSatisfy(title -> assertThat(title).containsIgnoringCase("title1"))
        );
    }

    @Test
    @DisplayName("제목을 통해 조회된 히어릿은 최신순으로 정렬된다.")
    void findByTitleContainingIgnoreCaseSortedDesc() {
        // given
        Hearit olderHearit = saveHearitByTitle("title");
        Hearit newerHearit = saveHearitByTitle("title");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.findByTitleOrderByCreatedAtDesc("title", pageable);

        // then
        List<Hearit> content = result.getContent();
        assertAll(
                () -> assertThat(content).hasSize(2),
                () -> assertThat(content.get(0).getId()).isEqualTo(newerHearit.getId()),
                () -> assertThat(content.get(1).getId()).isEqualTo(olderHearit.getId())
        );
    }

    @Test
    @DisplayName("제목을 통해 조회할 때 지정된 개수만큼 결과를 조회할 수 있다.")
    void findByTitleContainingIgnoreCaseWithPagination() {
        // given
        saveHearitByTitle("title1");
        saveHearitByTitle("title2");
        saveHearitByTitle("title3");

        Pageable pageable = PageRequest.of(1, 2); // 2개씩 끊어서 2페이지면 3번째 하나만 조회

        // when
        Page<Hearit> result = hearitRepository.findByTitleOrderByCreatedAtDesc("title", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(3)
        );
    }


    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix);
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
        Category category = new Category("category");
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

}
