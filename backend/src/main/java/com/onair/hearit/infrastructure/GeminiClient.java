package com.onair.hearit.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    public static final String API_URL = "/v1beta/models/gemini-2.5-flash:generateContent";
    private final RestClient restClient;
    private final String apiKey;

    public String createScript(String originText, String promptText) {
        String response = restClient.post()
                .uri(API_URL + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(GeminiRequest.of(originText, promptText))
                .retrieve()
                .body(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("파싱 에러");
        }
    }

    private record GeminiRequest(List<Content> contents) {
        public static GeminiRequest of(String originText, String promptText) {
            String inputText = promptText + originText;
            return new GeminiRequest(List.of(new Content(List.of(new Part(inputText)))));
        }
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }
}

