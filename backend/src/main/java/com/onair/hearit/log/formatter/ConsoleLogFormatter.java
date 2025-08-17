package com.onair.hearit.log.formatter;

import com.onair.hearit.log.dto.RequestLog;
import com.onair.hearit.log.dto.ResponseLog;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleLogFormatter {

    private static final List<String> SENSITIVE_KEYS = List.of(
            "password",
            "accessToken",
            "refreshToken",
            "url");

    public static String formatRequestLog(RequestLog requestLog) {
        String method = requestLog.getRequestInfo().getHttpMethod();
        String uri = requestLog.getRequestInfo().getRequestUri();
        String ip = requestLog.getRequestInfo().getIp();
        String time = requestLog.getTimestamp();
        Map<String, List<String>> params = requestLog.getRequestParameter();
        Object body = requestLog.getRequestBody();

        return String.format("[%s] %s → %s %s from %s params=%s body=%s",
                requestLog.getLogType(),
                time,
                method,
                uri,
                ip,
                toFlatParamString(params),
                truncateBody(body == null ? "null" : body.toString())
        );
    }

    private static String toFlatParamString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String truncateBody(String rawBody) {
        String masked = maskBodySensitiveData(rawBody);
        return masked.length() > 200 ? masked.substring(0, 200) + "...(생략)" : masked;
    }

    private static String maskBodySensitiveData(String raw) {
        for (String key : SENSITIVE_KEYS) {
            // record toString style: ClassName[field1=value1, key=value2, ...]
            String regex = String.format("(?i)(%s=)([^,\\]]+)", key);
            raw = raw.replaceAll(regex, "$1****");
        }
        return raw;
    }

    public static String formatResponseLog(ResponseLog<?> responseLog) {
        String time = responseLog.timestamp();
        String method = responseLog.requestInfo().getHttpMethod();
        String uri = responseLog.requestInfo().getRequestUri();
        String ip = responseLog.requestInfo().getIp();
        long timeTaken = responseLog.timeTakenMs();
        Object body = responseLog.responseBody();

        return String.format(
                "[%s] %s ← %s %s from %s timeTaken=%dms statusCode=%d body=%s",
                responseLog.logType(),
                time,
                method,
                uri,
                ip,
                timeTaken,
                responseLog.status(),
                truncateBody(body == null ? "null" : body.toString())
        );
    }
}
