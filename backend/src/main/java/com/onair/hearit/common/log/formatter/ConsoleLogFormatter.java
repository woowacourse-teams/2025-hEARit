package com.onair.hearit.common.log.formatter;

import com.onair.hearit.common.log.dto.RequestLog;
import com.onair.hearit.common.log.dto.ResponseLog;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleLogFormatter {


    public static String formatRequestLog(RequestLog requestLog) {
        String method = requestLog.getRequestInfo().getHttpMethod();
        String uri = requestLog.getRequestInfo().getRequestUri();
        String ip = requestLog.getRequestInfo().getIp();
        String time = requestLog.getTimestamp();
        Map<String, List<String>> params = requestLog.getRequestParameter();
        Object body = requestLog.getRequestBody();

        return String.format("[REQUEST] %s → %s %s from %s params=%s body=%s",
                time,
                method,
                uri,
                ip,
                toFlatParamString(params),
                body == null ? "null" : truncateBody(body.toString())
        );
    }

    private static String toFlatParamString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String truncateBody(String body) {
        return body.length() > 200 ? body.substring(0, 200) + "...(생ㅜ)" : body;
    }

    public static String formatResponseLog(ResponseLog<?> responseLog) {
        String time = responseLog.timestamp();
        String method = responseLog.requestInfo().getHttpMethod();
        String uri = responseLog.requestInfo().getRequestUri();
        String ip = responseLog.requestInfo().getIp();
        long timeTaken = responseLog.timeTakenMs();
        Object body = responseLog.responseBody();

        return String.format(
                "[RESPONSE] %s ← %s %s from %s timeTaken=%dms body=%s",
                time,
                method,
                uri,
                ip,
                timeTaken,
                truncateBody(body == null ? "null" : body.toString())
        );
    }
}
