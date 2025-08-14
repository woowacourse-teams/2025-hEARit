package com.onair.hearit.common.log.dto;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;

public record ResponseLog<T>(
        String logType,
        String timestamp,
        RequestInfo requestInfo,
        T responseEntity,
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
                responseEntity.getBody(),
                timeTakenMs);
    }
}
