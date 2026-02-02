package com.onair.hearit.admin.ai.exception;

import com.onair.hearit.admin.exception.AdminErrorCode;
import lombok.Getter;

@Getter
public class LlmResponseParseException extends LlmApiException {

    private final String rawResponse;

    public LlmResponseParseException(String detail, String rawResponse) {
        super(AdminErrorCode.AI_RESPONSE_PARSE_FAILED, detail);
        this.rawResponse = truncateResponse(rawResponse);
    }

    public LlmResponseParseException(String detail, String rawResponse, Throwable cause) {
        super(AdminErrorCode.AI_RESPONSE_PARSE_FAILED, detail, cause);
        this.rawResponse = truncateResponse(rawResponse);
    }

    private String truncateResponse(String response) {
        if (response == null) {
            return null;
        }
        int maxLength = 1000;
        if (response.length() <= maxLength) {
            return response;
        }
        return response.substring(0, maxLength) + "...(truncated)";
    }
}
