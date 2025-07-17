package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.infrastructure.HearitRepository;
import java.time.LocalDateTime;
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
class HearitServiceTest {

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    DbHelper dbHelper;

    HearitService hearitService;

    @BeforeEach
    void setup() {
        hearitService = new HearitService(hearitRepository);
    }

    @Test
    @DisplayName("히어릿 아이디로 단일 히어릿 정보를 조회 할 수 있다.")
    void getHearitDetailTest() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        HearitDetailResponse response = hearitService.getHearitDetail(hearit.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                () -> assertThat(response.title()).isEqualTo(hearit.getTitle()),
                () -> assertThat(response.summary()).isEqualTo(hearit.getSummary())
        );
    }

    @Test
    @DisplayName("존재하지 않는 히어릿 아이디로 단일 히어릿 조회 시 NoFoundException을 던진다.")
    void getHearitDetailNotFoundTest() {
        // given
        Long notExistHearitId = 1L;

        // when & then
        assertThatThrownBy(() -> hearitService.getHearitDetail(notExistHearitId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    private Hearit saveHearit(int num) {
        Category category = new Category("name" + num);
        dbHelper.insertCategory(category);

        Hearit hearit = new Hearit(
                "title" + num,
                "summary" + num, num,
                "originalAudioUrl" + num,
                "shortAudioUrl" + num,
                "scriptUrl" + num,
                "source" + num,
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }
}
