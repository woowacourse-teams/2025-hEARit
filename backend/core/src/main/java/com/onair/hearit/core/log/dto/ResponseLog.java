package com.onair.hearit.core.log.dto;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;

public record ResponseLog<T>(
        String logType,
        String timestamp,
        RequestInfo requestInfo,
        int status,
        T responseBody,
        long timeTakenMs
) {
    public static <T> ResponseLog<T> of(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            ResponseEntity<T> responseEntity,
            long timeTakenMs) {
        return new ResponseLog<>(
                "RESPONSE",
                timestamp.toString(),
                requestInfo,
                responseEntity.getStatusCode().value(),
                responseEntity.getBody(),
                timeTakenMs);
    }
}
