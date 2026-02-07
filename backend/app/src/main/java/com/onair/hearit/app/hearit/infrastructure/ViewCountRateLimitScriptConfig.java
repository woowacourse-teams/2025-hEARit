package com.onair.hearit.app.hearit.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/*
    조회수 증가의 중복 키 관리를 원자적으로 제어하기 위한 Lua Script 설정
 */
@Configuration
public class ViewCountRateLimitScriptConfig {

    @Bean
    public DefaultRedisScript<Long> viewCountLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        // Key가 이미 존재한다면 0(false)을 반환하여 중복 증가 방지
        // Key가 존재하지 않는 경우 N초 동안 증가 못하게 잠그는 Key 생성하고 1(true) 반환
        script.setResultType(Long.class);
        script.setScriptText("""
                    if redis.call('exists', KEYS[1]) == 1 then
                        return 0
                    else
                        redis.call('set', KEYS[1], '1', 'EX', ARGV[1])
                        return 1
                    end
                """);
        return script;
    }
}
