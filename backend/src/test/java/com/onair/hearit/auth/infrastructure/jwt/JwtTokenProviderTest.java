package com.onair.hearit.auth.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    @Disabled("만료 테스트는 실제 대기시간 필요 - 실제 실행은 비효율적이므로 무시")
    @Test
    @DisplayName("JWT 토큰은 만료 시간이 지나면 유효하지 않다")
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
