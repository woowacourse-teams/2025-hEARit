package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("integration-test")
@Sql("/dbclean.sql")
class CircuitBreakerRedisFailureIntegrationTest {

    static GenericContainer<?> redisContainer;

    @Autowired
    PlayingHistoryBufferFacade facade;

    @Autowired
    PlayingHistoryMapBuffer mapBuffer;

    @Autowired
    PlayingHistoryRedisBuffer redisBuffer;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    CircuitBreaker circuitBreaker;
    Member member;
    Category category;
    List<Hearit> hearits;

    @BeforeAll
    static void startRedis() {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getFirstMappedPort());
        registry.add("spring.data.redis.password", () -> "");
    }

    @AfterAll
    static void stopRedis() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @BeforeEach
    void setup() throws InterruptedException {
        // Redis 컨테이너 재시작 (이전 테스트에서 중지된 경우)
        if (!redisContainer.isRunning()) {
            redisContainer.start();
            waitForRedisUp();
        }

        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        // Map Buffer 초기화
        mapBuffer.clear();

        // Circuit Breaker 초기화
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisBuffer");
        circuitBreaker.reset(); // CLOSED 상태로 초기화

        // 테스트 데이터 준비
        member = dbHelper.insertMember(TestFixture.createFixedMember());
        category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        // 테스트용 Hearit 10개 생성
        hearits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            hearits.add(hearit);
        }
    }

    /**
     * Redis가 완전히 중단될 때까지 대기 (최대 3초).
     */
    private void waitForRedisDown() throws InterruptedException {
        for (int i = 0; i < 30; i++) {  // 최대 3초
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                Thread.sleep(100);  // Redis가 아직 살아있음, 계속 대기
            } catch (Exception e) {
                // Redis 연결 실패 → 중단 확인됨
                return;
            }
        }
    }

    /**
     * Redis가 정상 응답할 때까지 대기 (최대 5초).
     */
    private void waitForRedisUp() throws InterruptedException {
        for (int i = 0; i < 50; i++) {  // 최대 5초
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                // Redis 정상 응답 → 복구 확인됨
                return;
            } catch (Exception e) {
                Thread.sleep(100);  // Redis가 아직 응답 안 함, 계속 대기
            }
        }
        throw new IllegalStateException("Redis 재시작 실패");
    }

    private PlayingHistory createTestHistory(Member member, Hearit hearit) {
        return new PlayingHistory(member.getUuid(), hearit, 5000L);
    }

    @Test
    @DisplayName("Redis 장애 발생 시 Circuit이 OPEN되고 Map Buffer로 전환된다")
    void redis_failure_opens_circuit_and_uses_map_fallback() throws InterruptedException {
        // given
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // when: Redis 중단
        redisContainer.stop();
        waitForRedisDown();

        // 실패율 50%를 위해 4번 호출
        for (int i = 0; i < 4; i++) {
            PlayingHistory history = createTestHistory(member, hearits.get(i));
            facade.add(history, 1000L + i);
        }

        // then
        assertAll(
                () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN),
                () -> assertThat(mapBuffer.size()).isEqualTo(4), // Map Buffer에 저장됨
                () -> assertThat(redisBuffer.size()).isEqualTo(0) // Redis Buffer 사용X
        );

        // then: OPEN 상태에서도 flush/size 정상 동작
        assertDoesNotThrow(() -> facade.flush());
        assertDoesNotThrow(() -> facade.size());
    }

    @Test
    @Disabled
    @DisplayName("Redis 복구 후 Circuit이 CLOSED되고 Map Buffer가 자동 flush된다")
    void redis_recovery_closes_circuit_and_flushes_map() throws InterruptedException {
        // given: Circuit OPEN 상태
        circuitBreaker.transitionToOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        List<Long> expectedHearitIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PlayingHistory history = createTestHistory(member, hearits.get(i));
            facade.add(history, 1000L + i);
            expectedHearitIds.add(hearits.get(i).getId());
        }
        assertThat(mapBuffer.size()).isEqualTo(5);

        // when: Redis 재시작
        redisContainer.start();
        waitForRedisUp();
        Thread.sleep(1100);
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.HALF_OPEN);

        for (int i = 0; i < 3; i++) {
            PlayingHistory history = createTestHistory(member, hearits.get(5 + i));
            facade.add(history, 2000L + i);
        }

        // then: Circuit CLOSED
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 비동기 flush 완료 대기 (최대 5초)
        await().atMost(5, SECONDS)
                .pollInterval(100, MILLISECONDS)
                .untilAsserted(() -> {
                    assertThat(mapBuffer.size()).isEqualTo(0);
                });

        // DB에 저장 확인 (비동기 flush 완료 후 저장됨)
        await().atMost(3, SECONDS)
                .pollInterval(100, MILLISECONDS)
                .untilAsserted(() -> {
                    List<PlayingHistory> savedHistories =
                            playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(member.getUuid(), 100);
                    assertThat(savedHistories).hasSize(5);

                    List<Long> savedHearitIds = savedHistories.stream()
                            .map(PlayingHistory::getHearitId)
                            .toList();
                    assertThat(savedHearitIds).containsExactlyInAnyOrderElementsOf(expectedHearitIds);
                });
    }

    @Test
    @DisplayName("Circuit OPEN 상태에서도 관리 API(flush/size)는 차단되지 않는다")
    void circuit_open_management_apis_not_blocked() {
        // given: Circuit OPEN 상태
        circuitBreaker.transitionToOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        PlayingHistory history = createTestHistory(member, hearits.get(0));
        facade.add(history, 1000L);
        assertThat(mapBuffer.size()).isGreaterThan(0);

        // when & then: flush/size 정상 동작
        assertDoesNotThrow(() -> facade.flush());
        int size = assertDoesNotThrow(() -> facade.size());
        assertThat(size).isGreaterThanOrEqualTo(0);
    }
}
