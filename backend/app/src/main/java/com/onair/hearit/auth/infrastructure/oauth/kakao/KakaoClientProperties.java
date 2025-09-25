package com.onair.hearit.auth.infrastructure.oauth.kakao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties("kakao.user-info")
public class KakaoClientProperties {

    @NotBlank(message = "baseUrl은 비어있을 수 없습니다.")
    private final String baseUrl;

    @NotNull(message = "connectTimeout은 비어있을 수 없습니다.")
    private final Duration connectTimeout;

    @NotNull(message = "readTimeout은 비어있을 수 없습니다.")
    private final Duration readTimeout;
}
