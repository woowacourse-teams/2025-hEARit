package com.onair.hearit.common.log.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogMessageGenerator {

    private final ObjectMapper objectMapper;
    private final DefaultPrettyPrinter defaultPrettyPrinter;

    public String convertToPrettyJson(Object object) {
        try {
            return "\n" + objectMapper.writer(defaultPrettyPrinter).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 시, 에러 메시지를 포함한 객체의 toString() 결과를 반환하여 로깅 흐름이 끊기지 않도록 함
            return "Object to Json 직렬화 실패: " + object.toString();
        }
    }
}
