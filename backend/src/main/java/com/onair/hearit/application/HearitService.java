package com.onair.hearit.application;

import com.onair.hearit.dto.request.HearitCreationRequest;
import com.onair.hearit.dto.response.HearitCreationResponse;
import com.onair.hearit.infrastructure.GeminiClient;
import com.onair.hearit.infrastructure.GoogleCloudClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int MAX_TTS_BYTE_LENGTH = 4900; // GOOGLE TTS 바이트 제한 5000

    private final GeminiClient geminiClient;
    private final GoogleCloudClient googleCloudClient;

    public HearitCreationResponse addHearit(HearitCreationRequest request) {
        String allText = geminiClient.createScript(request.originText(), readPromptFile());
        Pattern pattern = Pattern.compile("title:\\s*(.*?)\\s*script:\\s*(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(allText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("title/script 형식을 찾을 수 없음");
        }

        String title = matcher.group(1).trim();
        String script = matcher.group(2).trim();

        List<String> chunks = splitByByteLength(script, MAX_TTS_BYTE_LENGTH);

        List<byte[]> audioChunks = chunks.stream()
                .map(googleCloudClient::generateAudio)
                .toList();

        String fileName = "hearit-" + UUID.randomUUID() + ".mp3";
        Path outputPath = Paths.get("hearit/audio/" + fileName);

        try {
            Files.createDirectories(outputPath.getParent());
            try (OutputStream os = Files.newOutputStream(outputPath)) {
                for (byte[] chunk : audioChunks) {
                    os.write(chunk);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("오디오 파일 저장 실패", e);
        }

        String audioUrl = "/hearit/audio/" + fileName;
        return HearitCreationResponse.from(title, script, audioUrl);
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

    private List<String> splitByByteLength(String text, int maxBytes) {
        List<String> result = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        int currentBytes = 0;

        for (char c : text.toCharArray()) {
            int charBytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            if (currentBytes + charBytes > maxBytes) {
                result.add(chunk.toString());
                chunk = new StringBuilder();
                currentBytes = 0;
            }
            chunk.append(c);
            currentBytes += charBytes;
        }

        if (!chunk.isEmpty()) {
            result.add(chunk.toString());
        }

        return result;
    }
}
