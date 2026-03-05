package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.exception.custom.RedisBufferException;
import com.onair.hearit.app.fixture.RedisContainerTestSupport;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PlayingHistoryBufferFacadeTest extends RedisContainerTestSupport {

    private PlayingHistoryRedisBuffer redisBuffer;
    private PlayingHistoryRedisBufferWithCircuitBreaker circuitWrappedRedisBuffer;
    private PlayingHistoryBufferFacade testFacade;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setup() {
        mapBuffer.clear();

        circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisBuffer");
        circuitBreaker.reset();

        redisBuffer = new PlayingHistoryRedisBuffer(
                redisTemplate,
                redissonClient,
                commandRepository,
                converter,
                new ObjectMapper(),
                1000L,  // lockWaitTime
                3000L   // lockLeaseTime
        );
        circuitWrappedRedisBuffer = new PlayingHistoryRedisBufferWithCircuitBreaker(redisBuffer, circuitBreaker);
        testFacade = new PlayingHistoryBufferFacade(circuitWrappedRedisBuffer, mapBuffer);
    }

    @AfterEach
    void cleanupAfterTest() {
        mapBuffer.clear();
    }

    @Nested
    @DisplayName("정상 상태 동작 (CLOSED)")
    class NormalOperation {

        @Test
        @DisplayName("CLOSED 상태에서는 정상적으로 Redis에 저장된다")
        void closed_usesRedis() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

            // when
            testFacade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED),
                    () -> assertThat(redisBuffer.size()).isEqualTo(1),
                    () -> assertThat(mapBuffer.size()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("CLOSED 상태에서 여러 요청이 모두 Redis에 저장된다")
        void closed_multipleRequests_allUseRedis() {
            // given
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

            int requestCount = 10;

            // when
            for (int i = 0; i < requestCount; i++) {
                Hearit hearit = dbHelper.insertHearit(
                        TestFixture.createFixedHearitWith(category)
                );
                testFacade.add(
                        new PlayingHistory(member.getUuid(), hearit, 1000L + i),
                        1000L + i
                );
            }

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED),
                    () -> assertThat(redisBuffer.size()).isEqualTo(requestCount),
                    () -> assertThat(mapBuffer.size()).isEqualTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Circuit OPEN 상태에서 fallback 동작")
    class CircuitOpenFallback {

        @Test
        @DisplayName("Circuit OPEN 시 Redis를 호출하지 않고 즉시 Map Buffer로 저장한다")
        void circuitOpen_immediatelyUsesMapFallback() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);

            // Circuit을 강제로 OPEN
            circuitBreaker.transitionToOpenState();

            // when
            testFacade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN),
                    () -> assertThat(mapBuffer.size()).isEqualTo(1),
                    () -> assertThat(redisBuffer.size()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("Circuit OPEN 상태에서 여러 요청이 모두 Map Buffer에 저장된다")
        void circuitOpen_multipleRequests_allUseMapBuffer() {
            // given
            circuitBreaker.transitionToOpenState();
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

            int requestCount = 10;

            // when
            for (int i = 0; i < requestCount; i++) {
                Hearit hearit = dbHelper.insertHearit(
                        TestFixture.createFixedHearitWith(category)
                );
                testFacade.add(
                        new PlayingHistory(member.getUuid(), hearit, 1000L + i),
                        1000L + i
                );
            }

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN),
                    () -> assertThat(mapBuffer.size()).isEqualTo(requestCount),
                    () -> assertThat(redisBuffer.size()).isEqualTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Redis 인프라 장애 시 fallback 동작")
    class RedisInfraFailureFallback {

        @Test
        @DisplayName("RedisBufferException 발생 시 Map Buffer로 fallback한다")
        void redisBufferException_usesFallback() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);

            // 임의로 RedisBufferException 발생시킴
            PlayingHistoryRedisBuffer spyRedisBuffer = spy(redisBuffer);
            doThrow(new RedisBufferException("Redis 연결 실패"))
                    .when(spyRedisBuffer).add(any(), anyLong());

            PlayingHistoryRedisBufferWithCircuitBreaker spyCircuitWrappedBuffer =
                    new PlayingHistoryRedisBufferWithCircuitBreaker(spyRedisBuffer, circuitBreaker);

            PlayingHistoryBufferFacade spyFacade = new PlayingHistoryBufferFacade(
                    spyCircuitWrappedBuffer,
                    mapBuffer
            );

            // when
            spyFacade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(redisBuffer.size()).isEqualTo(0),
                    () -> assertThat(mapBuffer.size()).isEqualTo(1),
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED)
            );
        }
    }
}
