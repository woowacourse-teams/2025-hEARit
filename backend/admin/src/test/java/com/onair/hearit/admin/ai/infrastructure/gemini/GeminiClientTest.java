package com.onair.hearit.admin.ai.infrastructure.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GeminiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        geminiClient = new GeminiClient(restTemplate, objectMapper);
        ReflectionTestUtils.setField(geminiClient, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiClient, "model", "gemini-2.5-flash");
    }

    @Nested
    @DisplayName("generateContent 메서드는")
    class GenerateContentTests {

        @Test
        @DisplayName("정상 응답에서 텍스트를 추출하여 반환한다")
        void returnsTextFromValidResponse() {
            // given
            String geminiResponse = """
                {
                    "candidates": [
                        {
                            "content": {
                                "parts": [
                                    {"text": "생성된 텍스트 응답"}
                                ]
                            }
                        }
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(geminiResponse, HttpStatus.OK));

            // when
            String result = geminiClient.generateContent("테스트 프롬프트");

            // then
            assertThat(result).isEqualTo("생성된 텍스트 응답");
        }

        @Test
        @DisplayName("candidates가 비어있으면 예외를 던진다")
        void throwsExceptionWhenCandidatesEmpty() {
            // given
            String emptyResponse = """
                {
                    "candidates": []
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(emptyResponse, HttpStatus.OK));

            // when & then
            assertThatThrownBy(() -> geminiClient.generateContent("프롬프트"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("Gemini 응답 형식 오류");
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

            // when & then
            assertThatThrownBy(() -> geminiClient.generateContent("프롬프트"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("LLM 처리 실패");
        }
    }

    @Nested
    @DisplayName("generateContentWithJson 메서드는")
    class GenerateContentWithJsonTests {

        @Test
        @DisplayName("JSON 응답을 텍스트로 반환한다")
        void returnsJsonResponseAsText() {
            // given
            String geminiResponse = """
                {
                    "candidates": [
                        {
                            "content": {
                                "parts": [
                                    {"text": "[{\\"id\\": 1, \\"text\\": \\"테스트\\"}]"}
                                ]
                            }
                        }
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(geminiResponse, HttpStatus.OK));

            // when
            String result = geminiClient.generateContentWithJson("JSON 응답 요청 프롬프트");

            // then
            assertThat(result).contains("[{");
            assertThat(result).contains("테스트");
        }

        @Test
        @DisplayName("parts가 비어있으면 예외를 던진다")
        void throwsExceptionWhenPartsEmpty() {
            // given
            String responseWithEmptyParts = """
                {
                    "candidates": [
                        {
                            "content": {
                                "parts": []
                            }
                        }
                    ]
                }
                """;

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>(responseWithEmptyParts, HttpStatus.OK));

            // when & then
            assertThatThrownBy(() -> geminiClient.generateContentWithJson("프롬프트"))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("Gemini 응답 형식 오류");
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

            // when & then
            assertThatThrownBy(() -> geminiClient.generateContentWithJson("프롬프트"))
                    .isInstanceOf(AudioProcessingException.class);
        }
    }

    @Nested
    @DisplayName("Rate Limiting 관련 테스트")
    class RateLimitingTests {

        @Test
        @DisplayName("429 에러 시 재시도 후 성공하면 결과를 반환한다")
        void retriesOn429AndSucceeds() {
            // given
            String successResponse = """
                {
                    "candidates": [
                        {
                            "content": {
                                "parts": [
                                    {"text": "재시도 성공"}
                                ]
                            }
                        }
                    ]
                }
                """;

            // 첫 번째 호출은 429, 두 번째 호출은 성공
            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(String.class)
            ))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "Too Many Requests",
                            null,
                            null,
                            null))
                    .thenReturn(new ResponseEntity<>(successResponse, HttpStatus.OK));

            // when
            String result = geminiClient.generateContent("프롬프트");

            // then
            assertThat(result).isEqualTo("재시도 성공");
        }
    }
}
