package com.onair.hearit.core.fixture;

import com.onair.hearit.common.TestClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "testDateTimeProvider")
public class TestJpaAuditingConfig {

    @Bean(name = "testDateTimeProvider")
    public DateTimeProvider testDateTimeProvider() {
        return new TestClock();
    }
}
