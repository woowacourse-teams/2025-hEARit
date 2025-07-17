package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.DbHelper;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
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
class FileSourceServiceTest {

    private static final String TEST_BASE_URL = "https://test.com";

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    private FileSourceService fileSourceService;

    @BeforeEach
    void setup() {
        fileSourceService = new FileSourceService(TEST_BASE_URL, hearitRepository);
    }

    @Test
    @DisplayName("히어릿 아이디로 요청 시 original audio url을 제공한다.")
    void getOriginalAudioTest() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        OriginalAudioResponse response = fileSourceService.getOriginalAudio(hearit.getId());

        // then
        assertThat(response.url()).isEqualTo(TEST_BASE_URL + hearit.getOriginalAudioUrl());
    }

    @Test
    @DisplayName("히어릿 아이디로 요청 시 short audio url을 제공한다.")
    void getShortAudioTest() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        ShortAudioResponse response = fileSourceService.getShortAudio(hearit.getId());

        // then
        assertThat(response.url()).isEqualTo(TEST_BASE_URL + hearit.getShortAudioUrl());
    }

    @Test
    @DisplayName("히어릿 아이디로 요청 시 script url을 제공한다.")
    void getScriptTest() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        ScriptResponse response = fileSourceService.getScript(hearit.getId());

        // then
        assertThat(response.url()).isEqualTo(TEST_BASE_URL + hearit.getScriptUrl());
    }

    @Test
    @DisplayName("존재하지 않는 히어릿 아이디 요청 시 NotFoundException을 던진다.")
    void notFoundHearitExceptionTest() {
        // given
        Long notSavedHearitId = 1L;

        // when & then
        assertThatThrownBy(() -> fileSourceService.getScript(notSavedHearitId))
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
