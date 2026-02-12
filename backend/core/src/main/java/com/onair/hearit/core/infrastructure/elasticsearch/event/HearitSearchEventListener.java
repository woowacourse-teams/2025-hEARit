package com.onair.hearit.core.infrastructure.elasticsearch.event;

import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearitSearchEventListener {
    private final ElasticsearchOperations operations;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(delay = 2000))
    public void handleHearitCreated(HearitCreatedEvent event) {
        HearitDocument document = new HearitDocument(
                event.id(), event.title(), event.summary(), event.keywords(), event.category(), event.createdAt());
        operations.save(document);
    }

    @Recover
    public void recover(Exception e, HearitCreatedEvent event) {
        log.error("[ES_SYNC_FATAL_ERROR] 3회 재시도 모두 실패. Hearit ID: {} - 사유: {}", event.id(), e.getMessage());
    }
}
