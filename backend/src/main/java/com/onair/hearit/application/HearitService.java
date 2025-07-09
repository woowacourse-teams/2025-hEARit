package com.onair.hearit.application;

import com.onair.hearit.dto.request.HearitCreationRequest;
import com.onair.hearit.dto.response.HearitCreationResponse;
import com.onair.hearit.infrastructure.GeminiClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private final GeminiClient geminiClient;

    public HearitCreationResponse addHearit(HearitCreationRequest request) {
        String allText = geminiClient.createScript(request.originText(), readPromptFile());
        Pattern pattern = Pattern.compile("title:\\s*(.*?)\\s*script:\\s*(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(allText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("title/script 형식을 찾을 수 없음");
        }

        String title = matcher.group(1).trim();
        String script = matcher.group(2).trim();

        // TODO: 오디오 추가
        return HearitCreationResponse.from(title, script, null);
    }

    private String readPromptFile() {
        ClassPathResource resource = new ClassPathResource("/prompt.txt");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            throw new IllegalArgumentException("prompt 파일 읽어오기 실패");
        }
    }
}
