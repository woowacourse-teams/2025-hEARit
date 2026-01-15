package com.onair.hearit.admin.ai.infrastructure.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class WhisperClient {

    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.whisper.model:whisper-1}")
    private String model;

    public WhisperClient(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 오디오 파일을 텍스트로 변환 (타임스탬프 포함)
     *
     * @param audioData 오디오 바이트 배열
     * @param filename 파일명 (확장자 포함)
     * @return TranscriptionResult (duration + segments)
     */
    public TranscriptionResult transcribe(byte[] audioData, String filename) {
        log.info("Whisper API 호출 시작: 파일={}, 크기={}KB", filename, audioData.length / 1024);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 파일 리소스 생성
        ByteArrayResource fileResource = new ByteArrayResource(audioData) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // 요청 바디 구성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("model", model);
        body.add("response_format", "verbose_json");  // 타임스탬프 포함
        body.add("language", "ko");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.postForEntity(
                    WHISPER_URL, requestEntity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Whisper API 응답 수신: {}ms 소요", elapsed);

            return parseWhisperResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("Whisper API 호출 실패", e);
            throw AudioProcessingException.apiCallFailed("STT 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Whisper API 응답을 ScriptSegment 형식으로 변환
     *
     * Whisper 응답 형식:
     * {
     *   "task": "transcribe",
     *   "language": "ko",
     *   "duration": 120.5,
     *   "segments": [
     *     {
     *       "id": 0,
     *       "start": 0.0,
     *       "end": 3.34,
     *       "text": " 텍스트",
     *       ...
     *     }
     *   ]
     * }
     */
    private TranscriptionResult parseWhisperResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 전체 재생 시간
            double duration = root.path("duration").asDouble(0);

            // segments 파싱
            JsonNode segmentsNode = root.path("segments");
            List<ScriptSegment> segments = new ArrayList<>();

            if (segmentsNode.isArray()) {
                for (JsonNode seg : segmentsNode) {
                    int id = seg.path("id").asInt();
                    double start = seg.path("start").asDouble();
                    double end = seg.path("end").asDouble();
                    String text = seg.path("text").asText("").trim();

                    if (!text.isEmpty()) {
                        segments.add(ScriptSegment.of(id, start, end, text));
                    }
                }
            }

            // duration이 없으면 마지막 segment의 end 사용
            if (duration == 0 && !segments.isEmpty()) {
                ScriptSegment lastSegment = segments.get(segments.size() - 1);
                duration = lastSegment.getEnd() / 1000.0;  // 밀리초 → 초
            }

            log.info("Whisper 파싱 완료: 재생시간={:.1f}초, 세그먼트={}개", duration, segments.size());

            return new TranscriptionResult(duration, segments);

        } catch (JsonProcessingException e) {
            log.error("Whisper 응답 파싱 실패: {}", jsonResponse, e);
            throw new AudioProcessingException("Whisper 응답 파싱 실패", e);
        }
    }

    /**
     * Whisper API 응답 결과
     */
    @Getter
    @AllArgsConstructor
    public static class TranscriptionResult {
        private final double duration;              // 전체 재생 시간 (초)
        private final List<ScriptSegment> segments; // 변환된 대본

        public int getDurationSeconds() {
            return (int) Math.ceil(duration);
        }
    }
}
