package com.onair.hearit.admin.ai.infrastructure.transcription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Groq Whisper API를 사용한 음성 변환 구현체
 */
@Component
@Slf4j
public class GroqSpeechTranscriber implements SpeechTranscriber {

    private static final String GROQ_WHISPER_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final int MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GroqSpeechTranscriber(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.whisper.model:whisper-large-v3-turbo}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public TranscriptionResult transcribe(byte[] audioData, String filename) {
        log.info("Groq Whisper API 호출 시작: 파일={}, 크기={}KB, 모델={}",
                filename, audioData.length / 1024, model);

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
                    GROQ_WHISPER_URL, requestEntity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Groq Whisper API 응답 수신: {}ms 소요", elapsed);

            return parseWhisperResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("Groq Whisper API 호출 실패", e);
            throw AudioProcessingException.apiCallFailed("STT 처리 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public int getMaxFileSizeBytes() {
        return MAX_FILE_SIZE;
    }

    /**
     * Whisper API 응답을 TranscriptionResult로 변환
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

            log.info("Groq Whisper 파싱 완료: 재생시간={}초, 세그먼트={}개",
                    String.format("%.1f", duration), segments.size());

            return new TranscriptionResult(duration, segments);

        } catch (JsonProcessingException e) {
            log.error("Groq Whisper 응답 파싱 실패: {}", jsonResponse, e);
            throw new AudioProcessingException("Whisper 응답 파싱 실패", e);
        }
    }
}
