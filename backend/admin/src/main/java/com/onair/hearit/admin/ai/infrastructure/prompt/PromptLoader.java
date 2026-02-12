package com.onair.hearit.admin.ai.infrastructure.prompt;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class PromptLoader {

    private final Resource scriptCorrectionResource;
    private final Resource metadataGenerationResource;

    private String scriptCorrectionPrompt;
    private String metadataGenerationPrompt;

    public PromptLoader(
            @Value("classpath:ai/prompts/script-correction.txt") Resource scriptCorrectionResource,
            @Value("classpath:ai/prompts/metadata-generation.txt") Resource metadataGenerationResource) {
        this.scriptCorrectionResource = scriptCorrectionResource;
        this.metadataGenerationResource = metadataGenerationResource;
    }

    @PostConstruct
    public void init() {
        this.scriptCorrectionPrompt = loadPrompt(scriptCorrectionResource, "script-correction");
        this.metadataGenerationPrompt = loadPrompt(metadataGenerationResource, "metadata-generation");
        log.info("AI 프롬프트 템플릿 로드 완료");
    }

    private String loadPrompt(Resource resource, String name) {
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("프롬프트 로드 완료: {}, 길이={}", name, content.length());
            return content;
        } catch (IOException e) {
            log.error("프롬프트 로드 실패: {}", name, e);
            throw new IllegalStateException("프롬프트 파일 로드 실패: " + name, e);
        }
    }

}
