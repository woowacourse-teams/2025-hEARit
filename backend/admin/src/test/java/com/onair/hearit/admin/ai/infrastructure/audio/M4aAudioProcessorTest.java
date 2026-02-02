package com.onair.hearit.admin.ai.infrastructure.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
class M4aAudioProcessorTest {

    @Mock
    private AudioClipper audioClipper;

    private M4aAudioProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new M4aAudioProcessor(audioClipper);
        ReflectionTestUtils.setField(processor, "shortsDurationSeconds", 60);
    }

    @Nested
    @DisplayName("supports 메서드는")
    class SupportsTests {

        @Test
        @DisplayName("m4a 확장자를 지원한다")
        void supportsM4aExtension() {
            assertThat(processor.supports("test.m4a")).isTrue();
        }

        @Test
        @DisplayName("aac 확장자를 지원한다")
        void supportsAacExtension() {
            assertThat(processor.supports("test.aac")).isTrue();
        }

        @Test
        @DisplayName("대소문자 상관없이 지원한다")
        void supportsUppercaseExtension() {
            assertThat(processor.supports("test.M4A")).isTrue();
            assertThat(processor.supports("test.AAC")).isTrue();
        }

        @Test
        @DisplayName("mp3 확장자는 지원하지 않는다")
        void doesNotSupportMp3() {
            assertThat(processor.supports("test.mp3")).isFalse();
        }

        @Test
        @DisplayName("null 파일명은 지원하지 않는다")
        void doesNotSupportNull() {
            assertThat(processor.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("validate 메서드는")
    class ValidateTests {

        @Test
        @DisplayName("파일 크기가 25MB를 초과하면 예외를 던진다")
        void throwsExceptionWhenFileTooLarge() {
            // given
            byte[] largeFile = new byte[26 * 1024 * 1024]; // 26MB
            addFtypBox(largeFile);

            // when & then
            assertThatThrownBy(() -> processor.validate(largeFile, "test.m4a"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("25MB");
        }

        @Test
        @DisplayName("확장자가 m4a/aac가 아니면 예외를 던진다")
        void throwsExceptionWhenNotM4aExtension() {
            // given
            byte[] m4aData = createValidM4aHeader();

            // when & then
            assertThatThrownBy(() -> processor.validate(m4aData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("M4A");
        }

        @Test
        @DisplayName("파일명이 null이면 예외를 던진다")
        void throwsExceptionWhenFilenameIsNull() {
            // given
            byte[] m4aData = createValidM4aHeader();

            // when & then
            assertThatThrownBy(() -> processor.validate(m4aData, null))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("M4A");
        }

        @Test
        @DisplayName("유효하지 않은 M4A 형식이면 예외를 던진다")
        void throwsExceptionWhenInvalidM4aFormat() {
            // given
            byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

            // when & then
            assertThatThrownBy(() -> processor.validate(invalidData, "test.m4a"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("유효하지 않은");
        }

        @Test
        @DisplayName("ftyp box가 있는 유효한 M4A를 통과시킨다")
        void passesValidM4aWithFtypBox() {
            // given
            byte[] m4aData = createValidM4aHeader();

            // when & then - 예외 없이 통과
            processor.validate(m4aData, "test.m4a");
        }

        @Test
        @DisplayName("isom 브랜드를 허용한다")
        void acceptsIsomBrand() {
            // given
            byte[] m4aData = createM4aHeaderWithBrand("isom");

            // when & then - 예외 없이 통과
            processor.validate(m4aData, "test.m4a");
        }
    }

    @Nested
    @DisplayName("getExtension 메서드는")
    class GetExtensionTests {

        @Test
        @DisplayName("m4a를 반환한다")
        void returnsM4a() {
            assertThat(processor.getExtension()).isEqualTo("m4a");
        }
    }

    @Nested
    @DisplayName("getMimeType 메서드는")
    class GetMimeTypeTests {

        @Test
        @DisplayName("audio/mp4를 반환한다")
        void returnsAudioMp4() {
            assertThat(processor.getMimeType()).isEqualTo("audio/mp4");
        }
    }

    @Nested
    @DisplayName("createShortClip 메서드는")
    class CreateShortClipTests {

        @Test
        @DisplayName("빈 데이터는 예외를 던진다")
        void throwsExceptionForEmptyData() {
            // given
            byte[] emptyData = new byte[0];

            // when & then
            assertThatThrownBy(() -> processor.createShortClip(emptyData, 60))
                    .isInstanceOf(AudioProcessingException.class);
        }

        @Test
        @DisplayName("null 데이터는 예외를 던진다")
        void throwsExceptionForNullData() {
            // when & then
            assertThatThrownBy(() -> processor.createShortClip(null, 60))
                    .isInstanceOf(AudioProcessingException.class);
        }

        @Test
        @DisplayName("AudioClipper를 통해 쇼츠를 생성한다")
        void callsAudioClipper() {
            // given
            byte[] m4aData = createValidM4aHeader();
            byte[] expectedResult = new byte[500];
            when(audioClipper.clip(any(byte[].class), eq("m4a"), eq(60)))
                    .thenReturn(expectedResult);

            // when
            byte[] result = processor.createShortClip(m4aData, 60);

            // then
            assertThat(result).isEqualTo(expectedResult);
            verify(audioClipper).clip(m4aData, "m4a", 60);
        }

        @Test
        @DisplayName("기본 쇼츠 길이를 사용한다")
        void usesDefaultShortsDuration() {
            // given
            byte[] m4aData = createValidM4aHeader();
            byte[] expectedResult = new byte[500];
            when(audioClipper.clip(any(byte[].class), eq("m4a"), eq(60)))
                    .thenReturn(expectedResult);

            // when
            byte[] result = processor.createShortClip(m4aData);

            // then
            verify(audioClipper).clip(m4aData, "m4a", 60);
        }
    }

    private byte[] createValidM4aHeader() {
        return createM4aHeaderWithBrand("M4A ");
    }

    private byte[] createM4aHeaderWithBrand(String brand) {
        byte[] data = new byte[100];
        // ftyp box: size(4) + "ftyp"(4) + brand(4)
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = 0x00;
        data[3] = 0x14; // size = 20
        data[4] = 'f';
        data[5] = 't';
        data[6] = 'y';
        data[7] = 'p';
        byte[] brandBytes = brand.getBytes();
        System.arraycopy(brandBytes, 0, data, 8, Math.min(4, brandBytes.length));
        return data;
    }

    private void addFtypBox(byte[] data) {
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = 0x00;
        data[3] = 0x14;
        data[4] = 'f';
        data[5] = 't';
        data[6] = 'y';
        data[7] = 'p';
        data[8] = 'M';
        data[9] = '4';
        data[10] = 'A';
        data[11] = ' ';
    }
}
