package com.onair.hearit.app.hearit.infrastructure;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
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
            Long result = redisTemplate.execute(viewCountLimitScript, List.of(key), String.valueOf(TTL_SECONDS));
            return result != null && result == 1L;
        } catch (RedisConnectionFailureException e) {
            // Redis 장애 시 조회 수로 인한 에러를 방지하기 위해 RDB로 Fallback 수행
            log.error("[Redis] connection error (fallback) - key: {}", key, e);
            return true;
        } catch (Exception e) {
            // NPE, 구문 문법 등 예상치 못한 오류는 로그를 남기고 디버깅을 위해 예외 발생
            log.error("[Redis] unexpected logic error - key: {}", key, e);
            throw e;
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
