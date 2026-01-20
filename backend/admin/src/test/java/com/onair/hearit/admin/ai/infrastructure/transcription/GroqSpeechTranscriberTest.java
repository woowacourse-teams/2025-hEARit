package com.onair.hearit.admin.ai.infrastructure.transcription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.transcription.SpeechTranscriber.TranscriptionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GroqSpeechTranscriberTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private GroqSpeechTranscriber speechTranscriber;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        speechTranscriber = new GroqSpeechTranscriber(
                restTemplate,
                objectMapper,
                "test-api-key",
                "whisper-large-v3-turbo"
        );
    }

    @Nested
    @DisplayName("transcribe 메서드는")
    class TranscribeTests {

        @Test
        @DisplayName("정상 응답을 파싱하여 TranscriptionResult를 반환한다")
        void returnsTranscriptionResultOnSuccess() {
            // given
            String jsonResponse = """
                {
                    "task": "transcribe",
                    "language": "ko",
                    "duration": 120.5,
                    "segments": [
                        {
                            "id": 0,
                            "start": 0.0,
                            "end": 5.5,
                            "text": "안녕하세요"
                        },
                        {
                            "id": 1,
                            "start": 5.5,
                            "end": 10.0,
                            "text": "테스트입니다"
                        }
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

            byte[] audioData = createTestAudioData();

            // when
            TranscriptionResult result = speechTranscriber.transcribe(audioData, "test.mp3");

            // then
            assertThat(result.getDuration()).isEqualTo(120.5);
            assertThat(result.getSegments()).hasSize(2);
            assertThat(result.getDurationSeconds()).isEqualTo(121); // ceil(120.5)

            ScriptSegment firstSegment = result.getSegments().get(0);
            assertThat(firstSegment.getId()).isZero();
            assertThat(firstSegment.getStart()).isEqualTo(0); // 초 → 밀리초 변환
            assertThat(firstSegment.getEnd()).isEqualTo(5500);
            assertThat(firstSegment.getText()).isEqualTo("안녕하세요");
        }

        @Test
        @DisplayName("duration이 없으면 마지막 segment의 end를 사용한다")
        void useLastSegmentEndWhenDurationMissing() {
            // given
            String jsonResponse = """
                {
                    "task": "transcribe",
                    "language": "ko",
                    "segments": [
                        {
                            "id": 0,
                            "start": 0.0,
                            "end": 30.0,
                            "text": "테스트"
                        }
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

            byte[] audioData = createTestAudioData();

            // when
            TranscriptionResult result = speechTranscriber.transcribe(audioData, "test.mp3");

            // then
            assertThat(result.getSegments()).hasSize(1);
        }

        @Test
        @DisplayName("빈 텍스트 세그먼트는 필터링된다")
        void filtersEmptyTextSegments() {
            // given
            String jsonResponse = """
                {
                    "duration": 10.0,
                    "segments": [
                        {"id": 0, "start": 0.0, "end": 5.0, "text": "유효한 텍스트"},
                        {"id": 1, "start": 5.0, "end": 7.0, "text": ""},
                        {"id": 2, "start": 7.0, "end": 10.0, "text": "  "}
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

            byte[] audioData = createTestAudioData();

            // when
            TranscriptionResult result = speechTranscriber.transcribe(audioData, "test.mp3");

            // then
            assertThat(result.getSegments()).hasSize(1);
            assertThat(result.getSegments().get(0).getText()).isEqualTo("유효한 텍스트");
        }

        @Test
        @DisplayName("API 호출 실패 시 AudioProcessingException을 던진다")
        void throwsExceptionOnApiFailure() {
            // given
            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenThrow(new RestClientException("Connection refused"));

            byte[] audioData = createTestAudioData();

            // when & then
            assertThatThrownBy(() -> speechTranscriber.transcribe(audioData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("STT 처리 실패");
        }

        @Test
        @DisplayName("잘못된 JSON 응답은 예외를 던진다")
        void throwsExceptionOnInvalidJson() {
            // given
            String invalidJson = "{ invalid json }";

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(invalidJson, HttpStatus.OK));

            byte[] audioData = createTestAudioData();

            // when & then
            assertThatThrownBy(() -> speechTranscriber.transcribe(audioData, "test.mp3"))
                    .isInstanceOf(AudioProcessingException.class);
        }
    }

    @Nested
    @DisplayName("getMaxFileSizeBytes 메서드는")
    class GetMaxFileSizeBytesTests {

        @Test
        @DisplayName("25MB를 반환한다")
        void returns25MB() {
            // when & then
            assertThat(speechTranscriber.getMaxFileSizeBytes())
                    .isEqualTo(25 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("TranscriptionResult는")
    class TranscriptionResultTests {

        @Test
        @DisplayName("getDurationSeconds는 올림 처리된 초를 반환한다")
        void getDurationSecondsReturnsCeiledValue() {
            // given
            TranscriptionResult result = new TranscriptionResult(120.1, java.util.List.of());

            // when & then
            assertThat(result.getDurationSeconds()).isEqualTo(121);
        }

        @Test
        @DisplayName("정확히 정수인 duration은 그대로 반환한다")
        void getDurationSecondsReturnsExactIntegerAsIs() {
            // given
            TranscriptionResult result = new TranscriptionResult(120.0, java.util.List.of());

            // when & then
            assertThat(result.getDurationSeconds()).isEqualTo(120);
        }
    }

    private byte[] createTestAudioData() {
        byte[] data = new byte[1000];
        // ID3v2 태그 시뮬레이션
        data[0] = 'I';
        data[1] = 'D';
        data[2] = '3';
        return data;
    }
}
