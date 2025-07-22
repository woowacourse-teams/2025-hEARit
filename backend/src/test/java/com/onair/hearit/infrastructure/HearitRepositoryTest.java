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

    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix, "#FFFFFFFF");
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
}
