package com.onair.hearit.admin.ai.infrastructure.transcription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.stt.SttProvider;
import com.onair.hearit.admin.ai.infrastructure.stt.SttResponse;
import com.onair.hearit.admin.ai.infrastructure.transcription.DefaultSpeechTranscriber.TranscriptionResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultSpeechTranscriber 테스트")
class DefaultSpeechTranscriberTest {

    @Mock
    private SttProvider sttProvider;

    private DefaultSpeechTranscriber transcriber;

    @BeforeEach
    void setUp() {
        transcriber = new DefaultSpeechTranscriber(sttProvider);
    }

    @Nested
    @DisplayName("transcribe 메서드")
    class TranscribeTest {

        @Test
        @DisplayName("정상적인 오디오 데이터로 변환 성공")
        void transcribe_withValidAudioData_shouldReturnResult() {
            // given
            byte[] audioData = new byte[]{1, 2, 3, 4, 5};
            String filename = "test.mp3";
            List<ScriptSegment> segments = List.of(
                    ScriptSegment.of(0, 0.0, 2.5, "안녕하세요"),
                    ScriptSegment.of(1, 2.5, 5.0, "반갑습니다")
            );

            SttResponse sttResponse = SttResponse.builder()
                    .duration(5.0)
                    .segments(segments)
                    .latencyMs(1500)
                    .provider("groq")
                    .build();

            when(sttProvider.transcribe(any())).thenReturn(sttResponse);

            // when
            TranscriptionResult result = transcriber.transcribe(audioData, filename);

            // then
            assertThat(result.getDuration()).isEqualTo(5.0);
            assertThat(result.getSegments()).hasSize(2);
            assertThat(result.getSegments().get(0).getText()).isEqualTo("안녕하세요");
            assertThat(result.getSegments().get(1).getText()).isEqualTo("반갑습니다");
        }

        @Test
        @DisplayName("빈 세그먼트 응답 처리")
        void transcribe_withEmptySegments_shouldReturnEmptyResult() {
            // given
            byte[] audioData = new byte[]{1, 2, 3};
            String filename = "empty.mp3";

            SttResponse sttResponse = SttResponse.builder()
                    .duration(0.0)
                    .segments(List.of())
                    .latencyMs(500)
                    .provider("groq")
                    .build();

            when(sttProvider.transcribe(any())).thenReturn(sttResponse);

            // when
            TranscriptionResult result = transcriber.transcribe(audioData, filename);

            // then
            assertThat(result.getDuration()).isEqualTo(0.0);
            assertThat(result.getSegments()).isEmpty();
        }

        @Test
        @DisplayName("null audioData로 호출 시 예외 발생")
        void transcribe_withNullAudioData_shouldThrowException() {
            // given
            byte[] audioData = null;
            String filename = "test.mp3";

            // when & then
            assertThatThrownBy(() -> transcriber.transcribe(audioData, filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("audioData");
        }

        @Test
        @DisplayName("빈 audioData로 호출 시 예외 발생")
        void transcribe_withEmptyAudioData_shouldThrowException() {
            // given
            byte[] audioData = new byte[0];
            String filename = "test.mp3";

            // when & then
            assertThatThrownBy(() -> transcriber.transcribe(audioData, filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("audioData");
        }

        @Test
        @DisplayName("null filename으로 호출 시 예외 발생")
        void transcribe_withNullFilename_shouldThrowException() {
            // given
            byte[] audioData = new byte[]{1, 2, 3};
            String filename = null;

            // when & then
            assertThatThrownBy(() -> transcriber.transcribe(audioData, filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("filename");
        }

        @Test
        @DisplayName("빈 filename으로 호출 시 예외 발생")
        void transcribe_withBlankFilename_shouldThrowException() {
            // given
            byte[] audioData = new byte[]{1, 2, 3};
            String filename = "   ";

            // when & then
            assertThatThrownBy(() -> transcriber.transcribe(audioData, filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("filename");
        }
    }

    @Nested
    @DisplayName("getMaxFileSizeBytes 메서드")
    class GetMaxFileSizeBytesTest {

        @Test
        @DisplayName("SttProvider의 최대 파일 크기 반환")
        void getMaxFileSizeBytes_shouldReturnProviderValue() {
            // given
            int expectedSize = 26214400;
            when(sttProvider.getMaxFileSizeBytes()).thenReturn(expectedSize);

            // when
            int result = transcriber.getMaxFileSizeBytes();

            // then
            assertThat(result).isEqualTo(expectedSize);
        }
    }
}
