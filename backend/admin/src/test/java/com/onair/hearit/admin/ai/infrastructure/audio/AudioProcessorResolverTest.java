package com.onair.hearit.admin.ai.infrastructure.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AudioProcessorResolverTest {

    @Mock
    private AudioProcessor mp3Processor;

    @Mock
    private AudioProcessor m4aProcessor;

    private AudioProcessorResolver resolver;

    @BeforeEach
    void setUp() {
        when(mp3Processor.supports("test.mp3")).thenReturn(true);
        when(mp3Processor.supports("test.m4a")).thenReturn(false);
        when(mp3Processor.getExtension()).thenReturn("mp3");

        when(m4aProcessor.supports("test.mp3")).thenReturn(false);
        when(m4aProcessor.supports("test.m4a")).thenReturn(true);
        when(m4aProcessor.getExtension()).thenReturn("m4a");

        resolver = new AudioProcessorResolver(List.of(mp3Processor, m4aProcessor));
    }

    @Nested
    @DisplayName("resolve 메서드는")
    class ResolveTests {

        @Test
        @DisplayName("mp3 파일에 대해 mp3 프로세서를 반환한다")
        void returnsMp3ProcessorForMp3File() {
            // when
            AudioProcessor result = resolver.resolve("test.mp3");

            // then
            assertThat(result).isEqualTo(mp3Processor);
        }

        @Test
        @DisplayName("m4a 파일에 대해 m4a 프로세서를 반환한다")
        void returnsM4aProcessorForM4aFile() {
            // when
            AudioProcessor result = resolver.resolve("test.m4a");

            // then
            assertThat(result).isEqualTo(m4aProcessor);
        }

        @Test
        @DisplayName("지원하지 않는 포맷이면 예외를 던진다")
        void throwsExceptionForUnsupportedFormat() {
            // given
            when(mp3Processor.supports("test.wav")).thenReturn(false);
            when(m4aProcessor.supports("test.wav")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> resolver.resolve("test.wav"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("지원하지 않는 오디오 형식");
        }
    }

    @Nested
    @DisplayName("getSupportedFormats 메서드는")
    class GetSupportedFormatsTests {

        @Test
        @DisplayName("모든 지원 포맷을 반환한다")
        void returnsAllSupportedFormats() {
            // when
            String formats = resolver.getSupportedFormats();

            // then
            assertThat(formats).contains("mp3");
            assertThat(formats).contains("m4a");
        }
    }

    @Nested
    @DisplayName("isSupported 메서드는")
    class IsSupportedTests {

        @Test
        @DisplayName("지원하는 포맷이면 true를 반환한다")
        void returnsTrueForSupportedFormat() {
            assertThat(resolver.isSupported("test.mp3")).isTrue();
            assertThat(resolver.isSupported("test.m4a")).isTrue();
        }

        @Test
        @DisplayName("지원하지 않는 포맷이면 false를 반환한다")
        void returnsFalseForUnsupportedFormat() {
            // given
            when(mp3Processor.supports("test.wav")).thenReturn(false);
            when(m4aProcessor.supports("test.wav")).thenReturn(false);

            // when & then
            assertThat(resolver.isSupported("test.wav")).isFalse();
        }
    }
}
