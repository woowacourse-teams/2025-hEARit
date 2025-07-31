package com.onair.hearit.auth.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    @DisplayName("JWT 토큰은 만료 시간이 지나면 유효하지 않다")
    @Test
    void jwt_token_invalid_after_expiry() throws InterruptedException {
        // given
        String shortSecretKey = "test-secret-key-123456789012345678901234567890";
        long shortExpiration = 1000L; // 1초

        JwtTokenProvider tokenProvider = new JwtTokenProvider(
                shortSecretKey,
                shortExpiration,
                60000L
        );

        String token = tokenProvider.createAccessToken(1L);

        // then
        assertThat(tokenProvider.validateToken(token)).isTrue();
        Thread.sleep(1000); // 토큰 만료될 때까지 1초 기다림
        assertThat(tokenProvider.validateToken(token)).isFalse();
    }
}
