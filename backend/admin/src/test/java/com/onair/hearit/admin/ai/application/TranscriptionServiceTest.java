package com.onair.hearit.admin.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.groq.GroqWhisperClient;
import com.onair.hearit.admin.ai.infrastructure.groq.GroqWhisperClient.TranscriptionResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptionServiceTest {

    @Mock
    private GroqWhisperClient whisperClient;

    private TranscriptionService transcriptionService;

    @BeforeEach
    void setUp() {
        transcriptionService = new TranscriptionService(whisperClient);
    }

    @Nested
    @DisplayName("transcribe 메서드는")
    class TranscribeTests {

        @Test
        @DisplayName("WhisperClient를 호출하고 결과를 반환한다")
        void callsWhisperClientAndReturnsResult() {
            // given
            byte[] audioData = createTestAudioData();
            String filename = "test.mp3";

            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 5000, "안녕하세요"),
                    new ScriptSegment(1, 5000, 10000, "테스트입니다")
            );
            TranscriptionResult expectedResult = new TranscriptionResult(10.0, segments);

            when(whisperClient.transcribe(any(byte[].class), anyString()))
                    .thenReturn(expectedResult);

            // when
            TranscriptionResult result = transcriptionService.transcribe(audioData, filename);

            // then
            assertThat(result.getDuration()).isEqualTo(10.0);
            assertThat(result.getSegments()).hasSize(2);
            verify(whisperClient).transcribe(audioData, filename);
        }

        @Test
        @DisplayName("재생시간을 올바르게 계산한다")
        void calculatesDurationCorrectly() {
            // given
            byte[] audioData = createTestAudioData();

            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 120500, "긴 오디오")
            );
            TranscriptionResult expectedResult = new TranscriptionResult(120.5, segments);

            when(whisperClient.transcribe(any(byte[].class), anyString()))
                    .thenReturn(expectedResult);

            // when
            TranscriptionResult result = transcriptionService.transcribe(audioData, "test.mp3");

            // then
            assertThat(result.getDurationSeconds()).isEqualTo(121); // ceil(120.5)
        }
    }

    @Nested
    @DisplayName("mergeSegmentsToText 메서드는")
    class MergeSegmentsToTextTests {

        @Test
        @DisplayName("세그먼트 텍스트를 공백으로 연결하여 반환한다")
        void mergesSegmentsWithSpace() {
            // given
            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 5000, "첫 번째"),
                    new ScriptSegment(1, 5000, 10000, "두 번째"),
                    new ScriptSegment(2, 10000, 15000, "세 번째")
            );

            // when
            String result = transcriptionService.mergeSegmentsToText(segments);

            // then
            assertThat(result).isEqualTo("첫 번째 두 번째 세 번째");
        }

        @Test
        @DisplayName("빈 텍스트 세그먼트는 건너뛴다")
        void skipsEmptyTextSegments() {
            // given
            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 5000, "유효한 텍스트"),
                    new ScriptSegment(1, 5000, 7000, ""),
                    new ScriptSegment(2, 7000, 10000, "또 다른 텍스트")
            );

            // when
            String result = transcriptionService.mergeSegmentsToText(segments);

            // then
            assertThat(result).isEqualTo("유효한 텍스트 또 다른 텍스트");
        }

        @Test
        @DisplayName("빈 세그먼트 목록은 빈 문자열을 반환한다")
        void returnsEmptyStringForEmptyList() {
            // given
            List<ScriptSegment> segments = List.of();

            // when
            String result = transcriptionService.mergeSegmentsToText(segments);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("단일 세그먼트도 올바르게 처리한다")
        void handlesSingleSegment() {
            // given
            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 5000, "단일 세그먼트")
            );

            // when
            String result = transcriptionService.mergeSegmentsToText(segments);

            // then
            assertThat(result).isEqualTo("단일 세그먼트");
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 결과는 trim 처리된다")
        void trimsResultString() {
            // given
            List<ScriptSegment> segments = List.of(
                    new ScriptSegment(0, 0, 5000, " 공백 있는 텍스트 ")
            );

            // when
            String result = transcriptionService.mergeSegmentsToText(segments);

            // then
            assertThat(result).isEqualTo("공백 있는 텍스트");
        }
    }

    private byte[] createTestAudioData() {
        byte[] data = new byte[1000];
        data[0] = 'I';
        data[1] = 'D';
        data[2] = '3';
        return data;
    }
}
