package com.onair.hearit.admin.ai.infrastructure.stt.groq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.stt.SttResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqResponseParser {

    private static final String PROVIDER_NAME = "groq";

    private final ObjectMapper objectMapper;

    public SttResponse parse(String jsonResponse, long latencyMs) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new LlmResponseParseException("Groq Whisper 응답이 비어있습니다", jsonResponse);
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            double duration = root.path("duration").asDouble(0);
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

            if (duration == 0 && !segments.isEmpty()) {
                ScriptSegment lastSegment = segments.get(segments.size() - 1);
                duration = lastSegment.getEnd() / 1000.0;
            }

            log.info("Groq Whisper 파싱 완료: 재생시간={}초, 세그먼트={}개",
                    String.format("%.1f", duration), segments.size());

            return SttResponse.builder()
                    .duration(duration)
                    .segments(segments)
                    .latencyMs(latencyMs)
                    .provider(PROVIDER_NAME)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Groq Whisper 응답 파싱 실패", e);
            throw new LlmResponseParseException("Groq Whisper 응답 파싱 실패", jsonResponse, e);
        }
    }
}
