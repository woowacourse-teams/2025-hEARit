package com.onair.hearit.auth.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KakaoClientProperties.class)
public class KakaoClientConfig {

    private final KakaoClientProperties kakaoClientProperties;

    @Bean
    public RestClient kakaoRestClient() {
        return RestClient.builder()
                .baseUrl(kakaoClientProperties.getBaseUrl())
                .build();
    }
}
