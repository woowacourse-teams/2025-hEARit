package com.onair.hearit.log.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestLog {

    private final String logType;
    private final String timestamp;
    private final RequestInfo requestInfo;
    private final Map<String, List<String>> requestParameter;
    private final Object requestBody;

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
        return new RequestLog("REQUEST", timestamp.toString(), requestInfo, parameters, body);
    }

    public static RequestLog ofFilter(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            Map<String, String[]> rawParameters) {
        Map<String, List<String>> parameters = rawParameters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue())
                ));
        return new RequestLog("ONLY_FILTER_REQUEST", timestamp.toString(), requestInfo, parameters, null); //Filter는 body 추적 불가
    }
}
