package com.onair.hearit.app.hearit.infrastructure;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountRateLimiter {

    private static final String KEY_PREFIX = "view:"; // 조회수 전용 Redis Key
    private static final long TTL_SECONDS = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> viewCountLimitScript;

    public boolean tryAcquireViewKey(UUID uuid, Long hearitId) {
        String key = KEY_PREFIX + uuid.toString() + ":" + hearitId;
        try {
            Long result = redisTemplate.execute(
                    viewCountLimitScript,
                    List.of(key),
                    String.valueOf(TTL_SECONDS)
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            /*
                Redis 장애 시 조회수 즉시 증가 시켜 추가적인 RDB 부하를 제거
             */
            return true;
        }
    }
}
