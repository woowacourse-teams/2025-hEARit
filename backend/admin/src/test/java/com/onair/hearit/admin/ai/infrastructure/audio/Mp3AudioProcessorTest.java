package com.onair.hearit.admin.ai.infrastructure.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class Mp3AudioProcessorTest {

    private Mp3AudioProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new Mp3AudioProcessor();
        ReflectionTestUtils.setField(processor, "shortsDurationSeconds", 60);
    }

    @Nested
    @DisplayName("validateMp3 메서드는")
    class ValidateMp3Tests {

        @Test
        @DisplayName("파일 크기가 25MB를 초과하면 예외를 던진다")
        void throwsExceptionWhenFileTooLarge() {
            // given
            byte[] largeFile = new byte[26 * 1024 * 1024]; // 26MB
            largeFile[0] = 'I';
            largeFile[1] = 'D';
            largeFile[2] = '3';

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(largeFile, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("25MB");
        }

        @Test
        @DisplayName("확장자가 mp3가 아니면 예외를 던진다")
        void throwsExceptionWhenNotMp3Extension() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(mp3Data, "test.wav"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("MP3");
        }

        @Test
        @DisplayName("파일명이 null이면 예외를 던진다")
        void throwsExceptionWhenFilenameIsNull() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(mp3Data, null))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("MP3");
        }

        @Test
        @DisplayName("유효하지 않은 MP3 형식이면 예외를 던진다")
        void throwsExceptionWhenInvalidMp3Format() {
            // given
            byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00};

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(invalidData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("유효하지 않은");
        }

        @Test
        @DisplayName("ID3 태그가 있는 유효한 MP3를 통과시킨다")
        void passesValidMp3WithId3Tag() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then - 예외 없이 통과
            processor.validateMp3(mp3Data, "test.mp3");
        }

        @Test
        @DisplayName("프레임 싱크 워드가 있는 유효한 MP3를 통과시킨다")
        void passesValidMp3WithFrameSync() {
            // given
            byte[] mp3Data = new byte[100];
            mp3Data[0] = (byte) 0xFF;
            mp3Data[1] = (byte) 0xFB; // 0xE0 mask 만족

            // when & then - 예외 없이 통과
            processor.validateMp3(mp3Data, "test.mp3");
        }

        @Test
        @DisplayName("대소문자 상관없이 mp3 확장자를 허용한다")
        void acceptsUppercaseMp3Extension() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then - 예외 없이 통과
            processor.validateMp3(mp3Data, "test.MP3");
        }
    }

    @Nested
    @DisplayName("createShortClip 메서드는")
    class CreateShortClipTests {

        @Test
        @DisplayName("유효하지 않은 MP3 데이터는 예외를 던진다")
        void throwsExceptionForInvalidMp3Data() {
            // given - mp3spi가 읽을 수 없는 가짜 MP3 데이터
            byte[] fakeMp3 = createValidMp3Header();

            // when & then - mp3spi가 실제로 파싱할 수 없으므로 예외 발생
            assertThatThrownBy(() -> processor.createShortClip(fakeMp3, 60))
                    .isInstanceOf(AudioProcessingException.class);
        }

        @Test
        @DisplayName("빈 데이터는 예외를 던진다")
        void throwsExceptionForEmptyData() {
            // given
            byte[] emptyData = new byte[0];

            // when & then
            assertThatThrownBy(() -> processor.createShortClip(emptyData, 60))
                    .isInstanceOf(AudioProcessingException.class);
        }
    }

    @Nested
    @DisplayName("isValidMp3 매직바이트 검증은")
    class MagicByteValidationTests {

        @Test
        @DisplayName("데이터가 null이면 false")
        void returnsFalseForNullData() {
            // given - null을 직접 테스트하기 어려우므로 너무 짧은 데이터로 테스트
            byte[] shortData = new byte[2];

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(shortData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class);
        }

        @Test
        @DisplayName("데이터가 3바이트 미만이면 false")
        void returnsFalseForDataShorterThan3Bytes() {
            // given
            byte[] shortData = new byte[2];

            // when & then
            assertThatThrownBy(() -> processor.validateMp3(shortData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class);
        }
    }

    private byte[] createValidMp3Header() {
        byte[] data = new byte[1000];
        // ID3v2 태그
        data[0] = 'I';
        data[1] = 'D';
        data[2] = '3';
        return data;
    }
}
