package com.onair.hearit.admin.ai.infrastructure.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class Mp3AudioProcessorTest {

    @Mock
    private AudioClipper audioClipper;

    private Mp3AudioProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new Mp3AudioProcessor(audioClipper);
        ReflectionTestUtils.setField(processor, "shortsDurationSeconds", 60);
    }

    @Nested
    @DisplayName("validate 메서드는")
    class ValidateTests {

        @Test
        @DisplayName("파일 크기가 25MB를 초과하면 예외를 던진다")
        void throwsExceptionWhenFileTooLarge() {
            // given
            byte[] largeFile = new byte[26 * 1024 * 1024]; // 26MB
            largeFile[0] = 'I';
            largeFile[1] = 'D';
            largeFile[2] = '3';

            // when & then
            assertThatThrownBy(() -> processor.validate(largeFile, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("25MB");
        }

        @Test
        @DisplayName("확장자가 mp3가 아니면 예외를 던진다")
        void throwsExceptionWhenNotMp3Extension() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then
            assertThatThrownBy(() -> processor.validate(mp3Data, "test.wav"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("MP3");
        }

        @Test
        @DisplayName("파일명이 null이면 예외를 던진다")
        void throwsExceptionWhenFilenameIsNull() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then
            assertThatThrownBy(() -> processor.validate(mp3Data, null))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("MP3");
        }

        @Test
        @DisplayName("유효하지 않은 MP3 형식이면 예외를 던진다")
        void throwsExceptionWhenInvalidMp3Format() {
            // given
            byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00};

            // when & then
            assertThatThrownBy(() -> processor.validate(invalidData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("유효하지 않은");
        }

        @Test
        @DisplayName("ID3 태그가 있는 유효한 MP3를 통과시킨다")
        void passesValidMp3WithId3Tag() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then - 예외 없이 통과
            processor.validate(mp3Data, "test.mp3");
        }

        @Test
        @DisplayName("프레임 싱크 워드가 있는 유효한 MP3를 통과시킨다")
        void passesValidMp3WithFrameSync() {
            // given
            byte[] mp3Data = new byte[100];
            mp3Data[0] = (byte) 0xFF;
            mp3Data[1] = (byte) 0xFB; // 0xE0 mask 만족

            // when & then - 예외 없이 통과
            processor.validate(mp3Data, "test.mp3");
        }

        @Test
        @DisplayName("대소문자 상관없이 mp3 확장자를 허용한다")
        void acceptsUppercaseMp3Extension() {
            // given
            byte[] mp3Data = createValidMp3Header();

            // when & then - 예외 없이 통과
            processor.validate(mp3Data, "test.MP3");
        }
    }

    @Nested
    @DisplayName("createShortClip 메서드는")
    class CreateShortClipTests {

        @Test
        @DisplayName("AudioClipper를 통해 쇼츠를 생성한다")
        void callsAudioClipper() {
            // given
            byte[] mp3Data = createValidMp3Header();
            byte[] expectedResult = new byte[500];
            when(audioClipper.clip(any(byte[].class), eq("mp3"), eq(60)))
                    .thenReturn(expectedResult);

            // when
            byte[] result = processor.createShortClip(mp3Data, 60);

            // then
            assertThat(result).isEqualTo(expectedResult);
            verify(audioClipper).clip(mp3Data, "mp3", 60);
        }

        @Test
        @DisplayName("기본 쇼츠 길이를 사용한다")
        void usesDefaultShortsDuration() {
            // given
            byte[] mp3Data = createValidMp3Header();
            byte[] expectedResult = new byte[500];
            when(audioClipper.clip(any(byte[].class), eq("mp3"), eq(60)))
                    .thenReturn(expectedResult);

            // when
            byte[] result = processor.createShortClip(mp3Data);

            // then
            verify(audioClipper).clip(mp3Data, "mp3", 60);
        }
    }

    @Nested
    @DisplayName("isValidMp3 매직바이트 검증은")
    class MagicByteValidationTests {

        @Test
        @DisplayName("데이터가 3바이트 미만이면 예외")
        void throwsExceptionForDataShorterThan3Bytes() {
            // given
            byte[] shortData = new byte[2];

            // when & then
            assertThatThrownBy(() -> processor.validate(shortData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class);
        }
    }

    @Nested
    @DisplayName("supports 메서드는")
    class SupportsTests {

        @Test
        @DisplayName("mp3 확장자를 지원한다")
        void supportsMp3Extension() {
            assertThat(processor.supports("test.mp3")).isTrue();
        }

        @Test
        @DisplayName("대소문자 상관없이 지원한다")
        void supportsUppercaseExtension() {
            assertThat(processor.supports("test.MP3")).isTrue();
        }

        @Test
        @DisplayName("m4a 확장자는 지원하지 않는다")
        void doesNotSupportM4a() {
            assertThat(processor.supports("test.m4a")).isFalse();
        }

        @Test
        @DisplayName("null 파일명은 지원하지 않는다")
        void doesNotSupportNull() {
            assertThat(processor.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getExtension/getMimeType 메서드는")
    class MetadataTests {

        @Test
        @DisplayName("mp3를 반환한다")
        void returnsCorrectExtension() {
            assertThat(processor.getExtension()).isEqualTo("mp3");
        }

        @Test
        @DisplayName("audio/mpeg를 반환한다")
        void returnsCorrectMimeType() {
            assertThat(processor.getMimeType()).isEqualTo("audio/mpeg");
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
