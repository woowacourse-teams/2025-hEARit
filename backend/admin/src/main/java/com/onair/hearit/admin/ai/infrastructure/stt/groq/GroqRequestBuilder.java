package com.onair.hearit.admin.ai.infrastructure.stt.groq;

import com.onair.hearit.admin.ai.config.SttProviderProperties.GroqProperties;
import com.onair.hearit.admin.ai.infrastructure.stt.SttRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class GroqRequestBuilder {

    private final GroqProperties properties;

    public MultiValueMap<String, Object> buildMultipartBody(SttRequest request) {
        ByteArrayResource fileResource = new ByteArrayResource(request.getAudioData()) {
            @Override
            public String getFilename() {
                return request.getFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("model", properties.getModel());
        body.add("response_format", "verbose_json");
        body.add("language", resolveLanguage(request));

        return body;
    }

    public String getApiKey() {
        return properties.getApiKey();
    }

    private String resolveLanguage(SttRequest request) {
        return request.getLanguage() != null
                ? request.getLanguage()
                : properties.getLanguage();
    }
}
