package com.onair.hearit.common.log.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonMaskingPrettyFormatter {

    private static final Map<String, Function<String, String>> MASKING_RULES = new HashMap<>() {{
        put("password", v -> "*****");
        put("localId", v -> maskEdge(v, 1));
        put("accessToken", v -> "*****");
    }};

    private final ObjectMapper objectMapper;
    private final DefaultPrettyPrinter defaultPrettyPrinter;

    /**
     * @param value
     * @param visible
     * @return value의 앞 뒤 1글자 제외하고 마스킹 된 String을 반환한다.
     */
    private static String maskEdge(String value, int visible) {
        if (value == null || value.length() <= visible * 2) {
            return "***";
        }
        return value.substring(0, visible) + "*".repeat(value.length() - visible * 2) +
                value.substring(value.length() - visible);
    }

    public String convertToPrettyJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            JsonNode root = objectMapper.readTree(json);
            applyMasking(root);
            return "\n" + objectMapper.writer(defaultPrettyPrinter).writeValueAsString(root);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 시, 에러 메시지를 포함한 객체의 toString() 결과를 반환하여 로깅 흐름이 끊기지 않도록 함
            return "Object to Json 직렬화 실패: " + object.toString();
        }
    }

    private void applyMasking(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode valueNode = entry.getValue();
                if (MASKING_RULES.containsKey(fieldName) && valueNode.isTextual()) {
                    String masked = MASKING_RULES.get(fieldName).apply(valueNode.asText());
                    objectNode.put(fieldName, masked);
                    continue;
                }
                applyMasking(valueNode);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                applyMasking(item);
            }
        }
    }
}
