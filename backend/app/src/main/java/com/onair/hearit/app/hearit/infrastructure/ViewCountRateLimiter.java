package com.onair.hearit.app.hearit.infrastructure;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountRateLimiter {

    private static final String KEY_PREFIX = "view:"; // 조회수 전용 Redis Key
    private static final long TTL_SECONDS = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> viewCountLimitScript;

    public boolean tryAcquireViewKey(UUID uuid, Long hearitId) {
        validateNull(uuid, hearitId);
        String key = KEY_PREFIX + uuid + ":" + hearitId;
        try {
            Long result = redisTemplate.execute(
                    viewCountLimitScript,
                    List.of(key),
                    String.valueOf(TTL_SECONDS)
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            /*
                Redis 장애 시:
                - 중복 키 검증은 포기
                - 조회수는 즉시 증가시켜 서비스 흐름 유지
             */
            log.warn("[Redis] 조회수 중복 키 처리를 위한 Redis 연결 실패 - fallback으로 조회수 증가 처리합니다. key={}", key, e);
            return true;
        }
    }

    private void validateNull(UUID uuid, Long hearitId) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID는 null이 될 수 없습니다.");
        }
        if (hearitId == null) {
            throw new IllegalArgumentException("hearitId는 null이 될 수 없습니다.");
        }
    }
}
