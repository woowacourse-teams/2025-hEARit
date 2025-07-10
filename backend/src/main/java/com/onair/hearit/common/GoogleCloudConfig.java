package com.onair.hearit.common;

import com.onair.hearit.infrastructure.GoogleCloudClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GoogleCloudConfig implements WebMvcConfigurer {

    private final String key;

    public GoogleCloudConfig(@Value("${google-cloud-api-key}") String key) {
        this.key = key;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/hearit/audio/**")
                .addResourceLocations("file:hearit/audio/");
    }

    @Bean
    public GoogleCloudClient googleCloudClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://texttospeech.googleapis.com")
                .build();
        return new GoogleCloudClient(restClient, key);
    }
}
