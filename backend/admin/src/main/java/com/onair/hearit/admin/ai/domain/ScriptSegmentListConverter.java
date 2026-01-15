package com.onair.hearit.admin.ai.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class ScriptSegmentListConverter implements AttributeConverter<List<ScriptSegment>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ScriptSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(segments);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert segments to JSON", e);
        }
    }

    @Override
    public List<ScriptSegment> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON to segments", e);
        }
    }
}
