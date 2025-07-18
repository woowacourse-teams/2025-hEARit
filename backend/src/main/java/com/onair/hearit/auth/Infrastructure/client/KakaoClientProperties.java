package com.onair.hearit.auth.Infrastructure.client;

import jakarta.validation.constraints.NotBlank;
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

    // TODO: timeout 관련 properties 설정 필요
//    @NotNull(message = "connectTimeout은 비어있을 수 없습니다.")
//    private final int connectTimeout;
//
//    @NotNull(message = "readTimeout은 비어있을 수 없습니다.")
//    private final int readTimeout;
//
//    @NotNull(message = "connectionRequestTimeout 비어있을 수 없습니다.")
//    private final int connectionRequestTimeout;
}
