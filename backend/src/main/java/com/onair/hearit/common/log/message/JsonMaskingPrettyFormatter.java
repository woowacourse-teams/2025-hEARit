package com.onair.hearit.common.log.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.apache.logging.log4j.message.ObjectMessage;
import org.springframework.stereotype.Component;

@Component
public class JsonMaskingPrettyFormatter {


    private final Map<String, Function<String, String>> maskingRules;
    private final ObjectMapper objectMapper;
//    private final DefaultPrettyPrinter defaultPrettyPrinter;

    public JsonMaskingPrettyFormatter(UrlMasker urlMasker, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.maskingRules = new HashMap<>() {{
            put("password", v -> "*****");
            put("localId", v -> maskEdge(v, 1));
            put("accessToken", v -> "*****");
            put("refreshToken", v -> "*****");
            put("url", v -> urlMasker.maskUrl(v));
        }};
    }

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

    public ObjectMessage convertToPrettyJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            JsonNode root = objectMapper.readTree(json);
            applyMasking(root);
            return new ObjectMessage(root);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 시, 에러 메시지를 포함한 객체의 toString() 결과를 반환하여 로깅 흐름이 끊기지 않도록 함
            return new ObjectMessage("Object to Json 직렬화 실패: " + object.toString());
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
                if (maskingRules.containsKey(fieldName) && valueNode.isTextual()) {
                    String masked = maskingRules.get(fieldName).apply(valueNode.asText());
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
