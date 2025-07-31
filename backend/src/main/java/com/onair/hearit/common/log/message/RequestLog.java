package com.onair.hearit.common.log.message;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RequestLog(
        String logType,
        LocalDateTime timestamp,
        RequestInfo requestInfo,
        Map<String, List<String>> requestParameter,
        Object requestBody
) {

    public static RequestLog of(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            Map<String, String[]> rawParameters,
            Object body) {
        Map<String, List<String>> parameters = rawParameters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue())
                ));
        return new RequestLog("REQUEST", timestamp, requestInfo, parameters, body);
    }
}