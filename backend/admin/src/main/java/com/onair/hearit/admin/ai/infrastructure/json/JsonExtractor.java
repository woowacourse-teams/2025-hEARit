package com.onair.hearit.admin.ai.infrastructure.json;

import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class JsonExtractor {

    private static final Pattern JSON_CODE_BLOCK =
            Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.MULTILINE);

    public String extractJsonArray(String response) {
        return tryExtractJsonArray(response)
                .orElseThrow(() -> new LlmResponseParseException(
                        "응답에서 유효한 JSON 배열을 찾을 수 없습니다", response));
    }

    public String extractJsonObject(String response) {
        return tryExtractJsonObject(response)
                .orElseThrow(() -> new LlmResponseParseException(
                        "응답에서 유효한 JSON 객체를 찾을 수 없습니다", response));
    }

    public Optional<String> tryExtractJsonArray(String response) {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        Optional<String> fromCodeBlock = extractFromCodeBlock(response);
        if (fromCodeBlock.isPresent() && fromCodeBlock.get().trim().startsWith("[")) {
            return fromCodeBlock;
        }

        return extractBalancedBrackets(response, '[', ']');
    }

    public Optional<String> tryExtractJsonObject(String response) {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        Optional<String> fromCodeBlock = extractFromCodeBlock(response);
        if (fromCodeBlock.isPresent() && fromCodeBlock.get().trim().startsWith("{")) {
            return fromCodeBlock;
        }

        return extractBalancedBrackets(response, '{', '}');
    }

    private Optional<String> extractFromCodeBlock(String response) {
        Matcher matcher = JSON_CODE_BLOCK.matcher(response);
        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        }
        return Optional.empty();
    }

    private Optional<String> extractBalancedBrackets(String response, char open, char close) {
        int start = response.indexOf(open);
        if (start == -1) {
            return Optional.empty();
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < response.length(); i++) {
            char c = response.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                    if (depth == 0) {
                        return Optional.of(response.substring(start, i + 1));
                    }
                }
            }
        }

        return Optional.empty();
    }
}
