package com.onair.hearit.auth.infrastructure.oauth.kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KakaoClientProperties.class)
public class KakaoClientConfig {

    private final KakaoClientProperties kakaoClientProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public RestClient kakaoRestClient() {
        return RestClient.builder()
                .requestFactory(getClientHttpRequestFactory())
                .defaultStatusHandler(new KakaoErrorHandler(objectMapper))
                .baseUrl(kakaoClientProperties.getBaseUrl())
                .build();
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(kakaoClientProperties.getConnectTimeout());
        requestFactory.setReadTimeout(kakaoClientProperties.getReadTimeout());
        return requestFactory;
    }
}
