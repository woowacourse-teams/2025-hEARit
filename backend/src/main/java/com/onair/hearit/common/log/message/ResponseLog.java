package com.onair.hearit.common.log.message;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;

public record ResponseLog(
        String logType,
        LocalDateTime timestamp,
        RequestInfo requestInfo,
        ResponseEntity<?> responseEntity,
        long timeTakenMs
) {

    public static ResponseLog of(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            ResponseEntity<?> responseEntity,
            long timeTakenMs) {
        return new ResponseLog(
                "RESPONSE",
                timestamp,
                requestInfo,
                responseEntity,
                timeTakenMs);
    }
}