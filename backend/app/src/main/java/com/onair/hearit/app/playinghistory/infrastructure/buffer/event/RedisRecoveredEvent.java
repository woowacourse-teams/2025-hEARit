package com.onair.hearit.app.playinghistory.infrastructure.buffer.event;

/**
 * Circuit Breaker가 HALF_OPEN → CLOSED 상태로 전환될 때 발행됨.
 */
public record RedisRecoveredEvent() {
}
