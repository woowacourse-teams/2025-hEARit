package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitExploreResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
class HearitServiceTest {

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private HearitService hearitService;

    @BeforeEach
    void setUp() {
        hearitService = new HearitService(hearitRepository);
    }

    @Test
    @DisplayName("최대 탐색 개수인 5개의 랜덤 히어릿을 조회할 수 있다.")
    void findByMemberId() {
        // given
        for (int i = 1; i <= 6; i++) {
            saveHearit(i);
        }

        // when
        List<HearitExploreResponse> hearits = hearitService.getExploredHearits();

        // then
        assertThat(hearits).hasSize(5);
    }

    private Hearit saveHearit(int num) {
        Category category = new Category("name" + num);
        categoryRepository.save(category);

        Hearit hearit = new Hearit("title" + num, "summary" + num, num, "originalAudioUrl" + num, "shortAudioUrl" + num,
                "scriptUrl" + num, "source" + num, LocalDateTime.now(), category);
        return hearitRepository.save(hearit);
    }
}
