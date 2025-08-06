package com.onair.hearit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.fixture.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HearitTest {

    @Nested
    @DisplayName("Hearit 검증 테스트")
    class HearitValidationTest {

        @ParameterizedTest
        @DisplayName("제목은 35자 이하의 문자열이야한다.")
        @ValueSource(strings = {"", "012345678901234567890123456789123456"})
        void titleValidationTest(String title) {
            // when & then
            assertThatThrownBy(
                    () -> new Hearit(title, "summary",
                            10, "ORG_123.mp3",
                            "SHR_123.mp3", "SCR_123.json",
                            "출처", TestFixture.createFixedCategory()))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("요약은 250자 이하의 문자열이어야한다.")
        void summaryValidationTest() {
            // given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 251; i++) {
                sb.append("a");
            }
            String summary = sb.toString();

            // when & then
            assertThatThrownBy(
                    () -> new Hearit("title", summary,
                            10, "ORG_123.mp3",
                            "SHR_123.mp3", "SCR_123.json",
                            "출처", TestFixture.createFixedCategory()))
                    .isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @DisplayName("총 길이는 1초 이상의 숫자여야한다.")
        @ValueSource(ints = {0, -1})
        void playTimeValidationTest(int playTime) {
            // when & then
            assertThatThrownBy(
                    () -> new Hearit("title", "summary",
                            playTime, "ORG_123.mp3",
                            "SHR_123.mp3", "SCR_123.json",
                            "출처", TestFixture.createFixedCategory()))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("출처는 250자 이하의 문자열이어야한다.")
        void sourceValidationTest() {
            // given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 251; i++) {
                sb.append("a");
            }
            String source = sb.toString();

            // when & then
            assertThatThrownBy(
                    () -> new Hearit("title", "summary",
                            10, "ORG_123.mp3",
                            "SHR_123.mp3", "SCR_123.json",
                            source, TestFixture.createFixedCategory()))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("카테고리는 반드시 입력해야한다.")
        void categoryValidationTest() {
            // when & then
            assertThatThrownBy(
                    () -> new Hearit("title", "summary",
                            10, "ORG_123.mp3",
                            "SHR_123.mp3", "SCR_123.json",
                            "출처", null))
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @ParameterizedTest
    @DisplayName("FileType에 따라 fileUrl을 수정한다.")
    @CsvSource({
            "ORIGINAL, ORG_123_new.mp3",
            "SHORT, SHR_123_new.mp3",
            "SCRIPT, SCR_123_new.json"})
    void updateFileUrlTest(FileType fileType, String fileUrl) {
        // given
        Hearit hearit = new Hearit("title", "summary",
                10, "/hearit/audio/original/ORG_123.mp3",
                "/hearit/audio/original/SHR_123.mp3", "/hearit/audio/original/SCR_123.json",
                "source", TestFixture.createFixedCategory());

        // when
        hearit.updateFileUrl(fileUrl, fileType);

        // then
        assertThat(hearit.getFileUrl(fileType)).isEqualTo(fileUrl);
    }
}
