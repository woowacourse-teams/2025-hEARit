package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
class HearitRepositoryTest {

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("원하는 개수만큼 랜덤 히어릿을 조회할 수 있다.")
    void findByMemberId() {
        // given
        saveHearit(1);
        saveHearit(2);

        Pageable pageable = PageRequest.of(0, 1);

        // when
        List<Hearit> hearits = hearitRepository.findRandom(pageable);

        // then
        assertAll(() -> {
            assertThat(hearits).hasSize(1);
            assertThat(hearitRepository.findAll()).hasSize(2);
        });
    }

    private Hearit saveHearit(int num) {
        Category category = new Category("name" + num);
        categoryRepository.save(category);

        Hearit hearit = new Hearit("title" + num, "summary" + num, num, "originalAudioUrl" + num, "shortAudioUrl" + num,
                "scriptUrl" + num, "source" + num, LocalDateTime.now(), category);
        return hearitRepository.save(hearit);
    }
}
